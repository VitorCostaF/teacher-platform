package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.dto.request.AdicionarAlunoRequest;
import br.com.inovadados.teacherplatform.dto.request.CriarTurmaRequest;
import br.com.inovadados.teacherplatform.dto.response.AlunoTurmaResponse;
import br.com.inovadados.teacherplatform.dto.response.ImportacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.TurmaDetalheResponse;
import br.com.inovadados.teacherplatform.dto.response.TurmaResumoResponse;
import br.com.inovadados.teacherplatform.exception.AcessoNegadoException;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.PeriodoLetivoRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PeriodoLetivoRepository periodoLetivoRepository;
    private final PlanilhaParserService planilhaParserService;

    @Transactional(readOnly = true)
    public List<TurmaResumoResponse> listarTurmasProfessor(UUID professorId, Long periodoId) {
        List<Turma> turmas = periodoId != null
                ? turmaRepository.findByProfessorIdAndPeriodoLetivoIdAndDeletadoEmIsNull(professorId, periodoId)
                : turmaRepository.findByProfessorIdAndDeletadoEmIsNull(professorId);
        return turmas.stream().map(this::toResumo).toList();
    }

    @Transactional(readOnly = true)
    public List<TurmaResumoResponse> listarTurmasAdmin(Long escolaId, Long periodoId) {
        List<Turma> turmas = periodoId != null
                ? turmaRepository.findByEscolaIdAndPeriodoLetivoIdAndDeletadoEmIsNull(escolaId, periodoId)
                : turmaRepository.findByEscolaIdAndDeletadoEmIsNull(escolaId);
        return turmas.stream().map(this::toResumo).toList();
    }

    @Transactional(readOnly = true)
    public TurmaDetalheResponse buscarTurma(Long turmaId, UUID usuarioId, boolean isAdmin) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));
        if (!isAdmin && !turma.getProfessor().getId().equals(usuarioId)) {
            throw new AcessoNegadoException();
        }
        int totalAlunos = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId).size();
        int naoCorrigidas = (int) entregaRepository.countByTurmaIdAndStatus(turmaId, StatusEntregaEnum.ENTREGUE);
        return new TurmaDetalheResponse(
                turma.getId(), turma.getNome(), turma.getDisciplina(), turma.getSerie(),
                totalAlunos, null, turma.getGradeHoraria(),
                turma.getPeriodoLetivo().getNome(), turma.getProfessor().getNome(),
                new TurmaResumoResponse.PendenciasDto(0, naoCorrigidas)
        );
    }

    public TurmaDetalheResponse criarTurma(CriarTurmaRequest req) {
        var periodo = periodoLetivoRepository.findById(req.periodoLetivoId())
                .orElseThrow(() -> new IllegalArgumentException("Período letivo não encontrado: " + req.periodoLetivoId()));
        var professor = usuarioRepository.findById(req.professorId())
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado: " + req.professorId()));

        Turma turma = new Turma();
        turma.setEscola(periodo.getEscola());
        turma.setPeriodoLetivo(periodo);
        turma.setProfessor(professor);
        turma.setNome(req.nome());
        turma.setDisciplina(req.disciplina());
        turma.setSerie(req.serie());
        turma.setGradeHoraria(req.gradeHoraria());
        turma = turmaRepository.save(turma);

        return new TurmaDetalheResponse(
                turma.getId(), turma.getNome(), turma.getDisciplina(), turma.getSerie(),
                0, null, turma.getGradeHoraria(), periodo.getNome(), professor.getNome(),
                new TurmaResumoResponse.PendenciasDto(0, 0)
        );
    }

    public TurmaDetalheResponse editarTurma(Long id, CriarTurmaRequest req) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new TurmaNaoEncontradaException(id));
        turma.setNome(req.nome());
        turma.setDisciplina(req.disciplina());
        turma.setSerie(req.serie());
        if (req.gradeHoraria() != null) turma.setGradeHoraria(req.gradeHoraria());
        turma = turmaRepository.save(turma);
        int totalAlunos = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(id).size();
        int naoCorrigidas = (int) entregaRepository.countByTurmaIdAndStatus(id, StatusEntregaEnum.ENTREGUE);
        return new TurmaDetalheResponse(
                turma.getId(), turma.getNome(), turma.getDisciplina(), turma.getSerie(),
                totalAlunos, null, turma.getGradeHoraria(),
                turma.getPeriodoLetivo().getNome(), turma.getProfessor().getNome(),
                new TurmaResumoResponse.PendenciasDto(0, naoCorrigidas)
        );
    }

    public void encerrarTurma(Long id) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new TurmaNaoEncontradaException(id));
        turma.setDeletadoEm(OffsetDateTime.now());
        turmaRepository.save(turma);
    }

    @Transactional(readOnly = true)
    public List<AlunoTurmaResponse> listarAlunos(Long turmaId) {
        return matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId).stream()
                .map(m -> new AlunoTurmaResponse(
                        m.getAluno().getId(),
                        m.getAluno().getNome(),
                        m.getAluno().getEmail(),
                        m.getAluno().getAvatarUrl(),
                        m.getMatriculadoEm().toLocalDate()
                ))
                .toList();
    }

    public AlunoTurmaResponse adicionarAluno(Long turmaId, AdicionarAlunoRequest req) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));

        Usuario aluno;
        if (req.alunoId() != null) {
            aluno = usuarioRepository.findById(req.alunoId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado: " + req.alunoId()));
        } else if (req.email() != null && !req.email().isBlank()) {
            aluno = usuarioRepository.findByEmail(req.email())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuário com e-mail '" + req.email() + "' não encontrado. Envie um convite para novos usuários."));
        } else {
            throw new IllegalArgumentException("Informe alunoId ou email");
        }

        Optional<Matricula> existente = matriculaRepository.findByTurmaIdAndAlunoId(turmaId, aluno.getId());
        if (existente.isPresent()) {
            Matricula m = existente.get();
            if (m.getRemovidoEm() == null) {
                throw new IllegalStateException("Aluno já matriculado nesta turma");
            }
            m.setRemovidoEm(null);
            m.setMatriculadoEm(OffsetDateTime.now());
            matriculaRepository.save(m);
            return toAlunoResponse(aluno, m.getMatriculadoEm());
        }

        Matricula matricula = new Matricula();
        matricula.setTurma(turma);
        matricula.setAluno(aluno);
        matricula.setMatriculadoEm(OffsetDateTime.now());
        matriculaRepository.save(matricula);
        return toAlunoResponse(aluno, matricula.getMatriculadoEm());
    }

    public void removerAluno(Long turmaId, UUID alunoId) {
        Matricula matricula = matriculaRepository.findByTurmaIdAndAlunoId(turmaId, alunoId)
                .filter(m -> m.getRemovidoEm() == null)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula ativa não encontrada para este aluno"));
        matricula.setRemovidoEm(OffsetDateTime.now());
        matriculaRepository.save(matricula);
    }

    public ImportacaoResponse importarAlunos(Long turmaId, MultipartFile file) throws IOException {
        turmaRepository.findById(turmaId).orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));

        List<Map<String, String>> rows = planilhaParserService.parsear(
                file.getInputStream(), file.getContentType(), file.getOriginalFilename()
        );

        int importados = 0;
        List<ImportacaoResponse.ErroImportacao> erros = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int linha = i + 2;
            String email = rows.get(i).get("email");
            if (email == null || email.isBlank()) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, "Campo 'email' ausente ou vazio"));
                continue;
            }
            try {
                adicionarAluno(turmaId, new AdicionarAlunoRequest(null, email.trim()));
                importados++;
            } catch (Exception e) {
                erros.add(new ImportacaoResponse.ErroImportacao(linha, e.getMessage()));
            }
        }

        return new ImportacaoResponse(importados, erros);
    }

    private TurmaResumoResponse toResumo(Turma turma) {
        int totalAlunos = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turma.getId()).size();
        int naoCorrigidas = (int) entregaRepository.countByTurmaIdAndStatus(turma.getId(), StatusEntregaEnum.ENTREGUE);
        return new TurmaResumoResponse(
                turma.getId(), turma.getNome(), turma.getDisciplina(),
                totalAlunos, null,
                new TurmaResumoResponse.PendenciasDto(0, naoCorrigidas)
        );
    }

    private AlunoTurmaResponse toAlunoResponse(Usuario aluno, OffsetDateTime matriculadoEm) {
        return new AlunoTurmaResponse(
                aluno.getId(), aluno.getNome(), aluno.getEmail(),
                aluno.getAvatarUrl(), matriculadoEm.toLocalDate()
        );
    }
}
