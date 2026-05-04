package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Escola;
import br.com.inovadados.teacherplatform.domain.entity.LogAuditoria;
import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.entity.TokenTemporario;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.entity.VinculoResponsavel;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoTokenEnum;
import br.com.inovadados.teacherplatform.dto.request.AlterarStatusProfessorRequest;
import br.com.inovadados.teacherplatform.dto.request.ConfiguracoesEscolaRequest;
import br.com.inovadados.teacherplatform.dto.request.ConvidarProfessorRequest;
import br.com.inovadados.teacherplatform.dto.request.CriarAlunoRequest;
import br.com.inovadados.teacherplatform.dto.request.TransferirAlunoRequest;
import br.com.inovadados.teacherplatform.dto.response.AdminDashboardResponse;
import br.com.inovadados.teacherplatform.dto.response.AlertaDashboardDto;
import br.com.inovadados.teacherplatform.dto.response.ImportacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.UsuarioResponse;
import br.com.inovadados.teacherplatform.exception.AcessoNegadoException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.EscolaRepository;
import br.com.inovadados.teacherplatform.repository.LogAuditoriaRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.RegistroFrequenciaRepository;
import br.com.inovadados.teacherplatform.repository.TokenTemporarioRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.repository.VinculoResponsavelRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EntregaRepository entregaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final TokenTemporarioRepository tokenTemporarioRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final VinculoResponsavelRepository vinculoResponsavelRepository;
    private final PlanilhaParserService planilhaParserService;
    private final AuditoriaService auditoriaService;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(Long escolaId) {
        long totalAlunos = usuarioRepository.countByEscolaIdAndPerfil(escolaId, PerfilEnum.ALUNO);
        long totalProfessores = usuarioRepository.countByEscolaIdAndPerfil(escolaId, PerfilEnum.PROFESSOR);
        List<Turma> turmas = turmaRepository.findByEscolaIdAndDeletadoEmIsNull(escolaId);
        long totalTurmas = turmas.size();

        List<Long> turmaIds = turmas.stream().map(Turma::getId).toList();
        List<Long> avaliacaoIds = avaliacaoRepository.findByTurmaIdIn(turmaIds).stream()
                .map(Avaliacao::getId).toList();

        List<BigDecimal> todasNotas = entregaRepository.findByAvaliacaoIdIn(avaliacaoIds).stream()
                .filter(e -> e.getNotaFinal() != null)
                .map(Entrega::getNotaFinal).toList();

        BigDecimal mediaNotas = calcularMedia(todasNotas);

        double somaFrequencias = 0;
        int turmasComAulas = 0;
        for (Turma t : turmas) {
            long totalAulas = frequenciaRepository.countAulasByTurmaId(t.getId());
            if (totalAulas == 0) continue;
            List<Matricula> matriculas = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(t.getId());
            long totalPresencas = matriculas.stream()
                    .mapToLong(m -> frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                            t.getId(), m.getAluno().getId(), StatusFrequenciaEnum.PRESENTE))
                    .sum();
            somaFrequencias += matriculas.isEmpty() ? 100 :
                    (double) totalPresencas / (matriculas.size() * totalAulas) * 100;
            turmasComAulas++;
        }
        BigDecimal frequenciaMedia = turmasComAulas == 0 ? BigDecimal.valueOf(100) :
                BigDecimal.valueOf(somaFrequencias / turmasComAulas).setScale(2, RoundingMode.HALF_UP);

        List<AdminDashboardResponse.DesempenhoSerieDto> desempenhoPorSerie =
                calcularDesempenhoPorSerie(turmas, avaliacaoIds);

        List<AlertaDashboardDto> alertas = gerarAlertasEscola(turmas);

        List<LogAuditoria> logs = logAuditoriaRepository.findByEscolaIdOrderByCriadoEmDesc(
                escolaId, PageRequest.of(0, 10));
        List<AdminDashboardResponse.AtividadeRecenteDto> atividadeRecente = logs.stream()
                .map(l -> new AdminDashboardResponse.AtividadeRecenteDto(
                        l.getAcao(), l.getEntidade(), l.getEntidadeId(), l.getCriadoEm()))
                .toList();

        return new AdminDashboardResponse(totalAlunos, totalProfessores, totalTurmas,
                mediaNotas, frequenciaMedia, desempenhoPorSerie, alertas, atividadeRecente);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarProfessores(Long escolaId, String nome, Boolean ativo, Pageable pageable) {
        return usuarioRepository.buscarPorEscolaEPerfil(escolaId, PerfilEnum.PROFESSOR, nome, ativo, pageable)
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getAvatarUrl()));
    }

    public UsuarioResponse convidarProfessor(ConvidarProfessorRequest req, Long escolaId,
                                             HttpServletRequest request) {
        if (usuarioRepository.findByEmail(req.email()).isPresent()) {
            throw new IllegalStateException("Já existe um usuário com este e-mail: " + req.email());
        }

        Escola escola = escolaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada"));

        Usuario professor = new Usuario();
        professor.setEscola(escola);
        professor.setNome(req.nome());
        professor.setEmail(req.email());
        professor.setPerfil(PerfilEnum.PROFESSOR);
        professor.setAtivo(false);
        professor.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        usuarioRepository.save(professor);

        String rawToken = UUID.randomUUID().toString();
        TokenTemporario token = new TokenTemporario();
        token.setUsuario(professor);
        token.setTipo(TipoTokenEnum.CONVITE);
        token.setTokenHash(hashSha256(rawToken));
        token.setExpiraEm(OffsetDateTime.now(ZoneOffset.UTC).plusDays(7));
        token.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        tokenTemporarioRepository.save(token);

        emailService.enviarConviteProfessor(professor.getEmail(), professor.getNome(),
                baseUrl + "/convite/" + rawToken);

        return new UsuarioResponse(professor.getId(), professor.getNome(), professor.getEmail(), null);
    }

    public UsuarioResponse alterarStatusProfessor(UUID professorId, AlterarStatusProfessorRequest req,
                                                   Long escolaId, UUID adminId,
                                                   HttpServletRequest request) {
        Usuario professor = usuarioRepository.findById(professorId)
                .filter(u -> u.getEscola().getId().equals(escolaId))
                .orElseThrow(() -> new AcessoNegadoException("Professor não encontrado nesta escola"));

        Map<String, Object> dadosAnteriores = Map.of("ativo", professor.getAtivo());
        professor.setAtivo(req.ativo());
        usuarioRepository.save(professor);

        auditoriaService.registrar(escolaId, adminId,
                req.ativo() ? "ATIVAR_PROFESSOR" : "DESATIVAR_PROFESSOR",
                "USUARIO", professorId.toString(),
                dadosAnteriores, Map.of("ativo", req.ativo()),
                req.motivo(), request);

        return new UsuarioResponse(professor.getId(), professor.getNome(), professor.getEmail(),
                professor.getAvatarUrl());
    }

    public ImportacaoResponse importarProfessores(MultipartFile file, Long escolaId,
                                                  HttpServletRequest request) throws IOException {
        List<Map<String, String>> rows = planilhaParserService.parsear(
                file.getInputStream(), file.getContentType(), file.getOriginalFilename());

        int importados = 0;
        List<ImportacaoResponse.ErroImportacao> erros = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int linha = i + 2;
            Map<String, String> row = rows.get(i);
            String email = row.get("email");
            String nome = row.get("nome");
            if (email == null || email.isBlank()) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, "Campo 'email' ausente"));
                continue;
            }
            if (nome == null || nome.isBlank()) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, "Campo 'nome' ausente"));
                continue;
            }
            try {
                convidarProfessor(new ConvidarProfessorRequest(nome.trim(), email.trim(), List.of()), escolaId, request);
                importados++;
            } catch (Exception e) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, e.getMessage()));
            }
        }

        return new ImportacaoResponse(importados, erros);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarAlunos(Long escolaId, String nome, Boolean ativo, Pageable pageable) {
        return usuarioRepository.buscarPorEscolaEPerfil(escolaId, PerfilEnum.ALUNO, nome, ativo, pageable)
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getAvatarUrl()));
    }

    public UsuarioResponse criarAluno(CriarAlunoRequest req, Long escolaId, UUID adminId,
                                      HttpServletRequest request) {
        if (usuarioRepository.findByEmail(req.email()).isPresent()) {
            throw new IllegalStateException("Já existe um usuário com este e-mail: " + req.email());
        }

        Escola escola = escolaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada"));

        Usuario aluno = new Usuario();
        aluno.setEscola(escola);
        aluno.setNome(req.nome());
        aluno.setEmail(req.email());
        aluno.setPerfil(PerfilEnum.ALUNO);
        aluno.setAtivo(false);
        aluno.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        usuarioRepository.save(aluno);

        for (Long turmaId : req.turmasIds()) {
            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));
            Matricula matricula = new Matricula();
            matricula.setTurma(turma);
            matricula.setAluno(aluno);
            matricula.setMatriculadoEm(OffsetDateTime.now(ZoneOffset.UTC));
            matriculaRepository.save(matricula);
        }

        if (req.responsaveis() != null) {
            for (CriarAlunoRequest.ResponsavelDto resp : req.responsaveis()) {
                Usuario responsavel = usuarioRepository.findByEmail(resp.email())
                        .orElseGet(() -> criarResponsavel(resp, escola));
                VinculoResponsavel vinculo = new VinculoResponsavel();
                vinculo.setResponsavel(responsavel);
                vinculo.setAluno(aluno);
                vinculo.setParentesco(resp.parentesco());
                vinculoResponsavelRepository.save(vinculo);
            }
        }

        String rawToken = UUID.randomUUID().toString();
        TokenTemporario token = new TokenTemporario();
        token.setUsuario(aluno);
        token.setTipo(TipoTokenEnum.CONVITE);
        token.setTokenHash(hashSha256(rawToken));
        token.setExpiraEm(OffsetDateTime.now(ZoneOffset.UTC).plusDays(7));
        token.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        tokenTemporarioRepository.save(token);

        emailService.enviarConviteAluno(aluno.getEmail(), aluno.getNome(),
                baseUrl + "/convite/" + rawToken);

        auditoriaService.registrar(escolaId, adminId, "CRIAR_ALUNO", "USUARIO",
                aluno.getId().toString(), null,
                Map.of("nome", aluno.getNome(), "email", aluno.getEmail()),
                null, request);

        return new UsuarioResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), null);
    }

    public ImportacaoResponse importarAlunos(MultipartFile file, Long escolaId, UUID adminId,
                                             HttpServletRequest request) throws IOException {
        List<Map<String, String>> rows = planilhaParserService.parsear(
                file.getInputStream(), file.getContentType(), file.getOriginalFilename());

        int importados = 0;
        List<ImportacaoResponse.ErroImportacao> erros = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int linha = i + 2;
            Map<String, String> row = rows.get(i);
            String email = row.get("email");
            String nome = row.get("nome");
            String turmaIdStr = row.get("turma_id");
            if (email == null || email.isBlank()) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, "Campo 'email' ausente"));
                continue;
            }
            if (nome == null || nome.isBlank()) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, "Campo 'nome' ausente"));
                continue;
            }
            if (turmaIdStr == null || turmaIdStr.isBlank()) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, "Campo 'turma_id' ausente"));
                continue;
            }
            try {
                Long turmaId = Long.parseLong(turmaIdStr.trim());
                criarAluno(new CriarAlunoRequest(nome.trim(), email.trim(), List.of(turmaId), null),
                        escolaId, adminId, request);
                importados++;
            } catch (Exception e) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, e.getMessage()));
            }
        }

        return new ImportacaoResponse(importados, erros);
    }

    public UsuarioResponse transferirAluno(UUID alunoId, TransferirAlunoRequest req,
                                           Long escolaId, UUID adminId,
                                           HttpServletRequest request) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .filter(u -> u.getEscola().getId().equals(escolaId))
                .orElseThrow(() -> new AcessoNegadoException("Aluno não encontrado nesta escola"));

        List<Matricula> ativas = matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId);
        List<Long> turmasAnteriores = ativas.stream().map(m -> m.getTurma().getId()).toList();
        ativas.forEach(m -> {
            m.setRemovidoEm(OffsetDateTime.now(ZoneOffset.UTC));
            matriculaRepository.save(m);
        });

        Turma novaTurma = turmaRepository.findById(req.novaTurmaId())
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + req.novaTurmaId()));
        Matricula novaMatricula = new Matricula();
        novaMatricula.setTurma(novaTurma);
        novaMatricula.setAluno(aluno);
        novaMatricula.setMatriculadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        matriculaRepository.save(novaMatricula);

        auditoriaService.registrar(escolaId, adminId, "TRANSFERIR_ALUNO", "MATRICULA",
                alunoId.toString(),
                Map.of("turmasAnteriores", turmasAnteriores),
                Map.of("novaTurmaId", req.novaTurmaId()),
                null, request);

        return new UsuarioResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getAvatarUrl());
    }

    @Transactional(readOnly = true)
    public Escola getConfiguracoes(Long escolaId) {
        return escolaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada"));
    }

    public Escola atualizarConfiguracoes(Long escolaId, ConfiguracoesEscolaRequest req,
                                         UUID adminId, HttpServletRequest request) {
        Escola escola = escolaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada"));

        Map<String, Object> dadosAnteriores = Map.of(
                "nome", escola.getNome(),
                "notaMinimaAprovacao", escola.getNotaMinimaAprovacao(),
                "frequenciaMinimaAprovacao", escola.getFrequenciaMinimaAprovacao(),
                "sistemaAvaliacao", escola.getSistemaAvaliacao()
        );

        escola.setNome(req.nome());
        escola.setNotaMinimaAprovacao(req.notaMinimaAprovacao());
        escola.setFrequenciaMinimaAprovacao(req.frequenciaMinimaAprovacao());
        escola.setSistemaAvaliacao(req.sistemaAvaliacao());
        escola.setAtualizadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        escolaRepository.save(escola);

        auditoriaService.registrar(escolaId, adminId, "ATUALIZAR_CONFIGURACOES", "ESCOLA",
                escolaId.toString(), dadosAnteriores,
                Map.of("nome", req.nome(), "sistemaAvaliacao", req.sistemaAvaliacao()),
                null, request);

        return escola;
    }

    private Usuario criarResponsavel(CriarAlunoRequest.ResponsavelDto resp, Escola escola) {
        Usuario responsavel = new Usuario();
        responsavel.setEscola(escola);
        responsavel.setNome(resp.nome());
        responsavel.setEmail(resp.email());
        responsavel.setPerfil(PerfilEnum.RESPONSAVEL);
        responsavel.setAtivo(false);
        responsavel.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        usuarioRepository.save(responsavel);

        String rawToken = UUID.randomUUID().toString();
        TokenTemporario token = new TokenTemporario();
        token.setUsuario(responsavel);
        token.setTipo(TipoTokenEnum.CONVITE);
        token.setTokenHash(hashSha256(rawToken));
        token.setExpiraEm(OffsetDateTime.now(ZoneOffset.UTC).plusDays(7));
        token.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        tokenTemporarioRepository.save(token);

        emailService.enviarConviteResponsavel(responsavel.getEmail(), responsavel.getNome(),
                baseUrl + "/convite/" + rawToken);

        return responsavel;
    }

    private List<AdminDashboardResponse.DesempenhoSerieDto> calcularDesempenhoPorSerie(
            List<Turma> turmas, List<Long> avaliacaoIds) {
        Map<String, List<BigDecimal>> notasPorSerie = new HashMap<>();
        Map<String, Integer> alunosPorSerie = new HashMap<>();

        Map<Long, String> serieParaTurma = turmas.stream()
                .collect(Collectors.toMap(Turma::getId, Turma::getSerie));

        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdIn(
                turmas.stream().map(Turma::getId).toList());
        Map<Long, Long> turmaParaAvaliacao = avaliacoes.stream()
                .collect(Collectors.toMap(Avaliacao::getId, a -> a.getTurma().getId(), (a, b) -> a));

        entregaRepository.findByAvaliacaoIdIn(avaliacaoIds).stream()
                .filter(e -> e.getNotaFinal() != null)
                .forEach(e -> {
                    Long turmaId = turmaParaAvaliacao.get(e.getAvaliacao().getId());
                    String serie = turmaId != null ? serieParaTurma.get(turmaId) : null;
                    if (serie != null) {
                        notasPorSerie.computeIfAbsent(serie, k -> new ArrayList<>())
                                .add(e.getNotaFinal());
                    }
                });

        turmas.forEach(t -> {
            int count = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(t.getId()).size();
            alunosPorSerie.merge(t.getSerie(), count, Integer::sum);
        });

        return notasPorSerie.entrySet().stream()
                .map(e -> new AdminDashboardResponse.DesempenhoSerieDto(
                        e.getKey(), calcularMedia(e.getValue()),
                        alunosPorSerie.getOrDefault(e.getKey(), 0)))
                .sorted(Comparator.comparing(AdminDashboardResponse.DesempenhoSerieDto::serie))
                .toList();
    }

    private List<AlertaDashboardDto> gerarAlertasEscola(List<Turma> turmas) {
        List<AlertaDashboardDto> alertas = new ArrayList<>();
        for (Turma turma : turmas) {
            List<Matricula> matriculas = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turma.getId());
            long totalAulas = frequenciaRepository.countAulasByTurmaId(turma.getId());
            if (totalAulas == 0) continue;
            for (Matricula m : matriculas) {
                long ausencias = frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                        turma.getId(), m.getAluno().getId(), StatusFrequenciaEnum.AUSENTE);
                double pct = (double) ausencias / totalAulas * 100;
                if (pct >= 25) {
                    alertas.add(new AlertaDashboardDto("RISCO_REPROVACAO",
                            m.getAluno().getNome() + " com " + String.format("%.0f", pct) + "% de faltas em " + turma.getNome(),
                            turma.getId(),
                            "/turmas/" + turma.getId() + "/alunos/" + m.getAluno().getId() + "/desempenho"));
                }
            }
        }
        return alertas;
    }

    private BigDecimal calcularMedia(List<BigDecimal> notas) {
        if (notas.isEmpty()) return BigDecimal.ZERO;
        return notas.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private String hashSha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
