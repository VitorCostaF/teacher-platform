package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Questao;
import br.com.inovadados.teacherplatform.domain.entity.Resposta;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoAtividadeRequest;
import br.com.inovadados.teacherplatform.dto.response.AnaliseTopicoDto;
import br.com.inovadados.teacherplatform.dto.response.AtividadeDetalheResponse;
import br.com.inovadados.teacherplatform.dto.response.EntregarAtividadeResponse;
import br.com.inovadados.teacherplatform.dto.response.GabaritoQuestaoDto;
import br.com.inovadados.teacherplatform.dto.response.QuestaoAtividadeDto;
import br.com.inovadados.teacherplatform.dto.response.ResultadoEntregaResponse;
import br.com.inovadados.teacherplatform.exception.AvaliacaoNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.OperacaoNaoPermitidaException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.QuestaoRepository;
import br.com.inovadados.teacherplatform.repository.RespostaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AtividadeService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final EntregaRepository entregaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AtividadeDetalheResponse getAtividade(Long avaliacaoId, UUID alunoId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(avaliacaoId));

        if (avaliacao.getStatus() != StatusAvaliacaoEnum.PUBLICADA
                && avaliacao.getStatus() != StatusAvaliacaoEnum.ENCERRADA) {
            throw new OperacaoNaoPermitidaException("Avaliação não disponível");
        }

        List<Questao> questoes = questaoRepository.findByAvaliacaoIdOrderByOrdem(avaliacaoId);
        Entrega entrega = entregaRepository.findByAvaliacaoIdAndAlunoId(avaliacaoId, alunoId).orElse(null);

        String statusAluno = resolverStatus(entrega);
        Map<Long, Object> respostasRascunho = new HashMap<>();

        if (entrega != null && entrega.getStatus() == StatusEntregaEnum.RASCUNHO) {
            respostasRascunho = carregarRespostas(entrega.getId());
        }

        boolean gabaritoDisponivel = isGabaritoDisponivel(avaliacao);

        return new AtividadeDetalheResponse(
                avaliacao.getId(),
                avaliacao.getTitulo(),
                avaliacao.getTurma().getDisciplina(),
                avaliacao.getTipo().name(),
                avaliacao.getEncerraEm(),
                avaliacao.isPermiteEntregaAtrasada(),
                gabaritoDisponivel,
                statusAluno,
                questoes.stream().map(this::toQuestaoDto).toList(),
                respostasRascunho
        );
    }

    public void salvarRascunho(Long avaliacaoId, SalvarRascunhoAtividadeRequest req, UUID alunoId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(avaliacaoId));

        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        Entrega entrega = entregaRepository.findByAvaliacaoIdAndAlunoId(avaliacaoId, alunoId)
                .orElseGet(() -> criarEntrega(avaliacao, aluno));

        if (entrega.getStatus() == StatusEntregaEnum.ENTREGUE
                || entrega.getStatus() == StatusEntregaEnum.CORRIGIDA) {
            throw new OperacaoNaoPermitidaException("Atividade já entregue");
        }

        if (entrega.getStatus() == StatusEntregaEnum.NAO_INICIADA) {
            entrega.setStatus(StatusEntregaEnum.RASCUNHO);
            entrega.setIniciadoEm(OffsetDateTime.now());
        }

        entregaRepository.save(entrega);
        salvarRespostas(entrega, req.respostas(), questaoRepository.findByAvaliacaoIdOrderByOrdem(avaliacaoId));
    }

    public EntregarAtividadeResponse entregar(Long avaliacaoId, SalvarRascunhoAtividadeRequest req, UUID alunoId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(avaliacaoId));

        OffsetDateTime agora = OffsetDateTime.now();
        boolean atrasada = avaliacao.getEncerraEm() != null && avaliacao.getEncerraEm().isBefore(agora);

        if (atrasada && !avaliacao.isPermiteEntregaAtrasada()) {
            throw new OperacaoNaoPermitidaException("Prazo de entrega encerrado");
        }

        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        Entrega entrega = entregaRepository.findByAvaliacaoIdAndAlunoId(avaliacaoId, alunoId)
                .orElseGet(() -> criarEntrega(avaliacao, aluno));

        if (entrega.getStatus() == StatusEntregaEnum.ENTREGUE
                || entrega.getStatus() == StatusEntregaEnum.CORRIGIDA) {
            throw new OperacaoNaoPermitidaException("Atividade já entregue");
        }

        List<Questao> questoes = questaoRepository.findByAvaliacaoIdOrderByOrdem(avaliacaoId);
        salvarRespostas(entrega, req.respostas(), questoes);

        BigDecimal nota = calcularNotaAutomatica(entrega.getId(), questoes, req.respostas());
        entrega.setNotaAutomatica(nota);
        entrega.setNotaFinal(nota);
        entrega.setStatus(StatusEntregaEnum.ENTREGUE);
        entrega.setEntregueEm(agora);
        entrega.setEntregaAtrasada(atrasada);
        if (entrega.getIniciadoEm() == null) entrega.setIniciadoEm(agora);
        entregaRepository.save(entrega);

        boolean gabaritoDisponivel = isGabaritoDisponivel(avaliacao);
        List<GabaritoQuestaoDto> gabarito = gabaritoDisponivel
                ? montarGabarito(entrega.getId(), questoes, req.respostas())
                : List.of();

        return new EntregarAtividadeResponse(entrega.getId(), nota, gabaritoDisponivel, gabarito);
    }

    @Transactional(readOnly = true)
    public ResultadoEntregaResponse getResultado(Long entregaId, UUID alunoId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Entrega não encontrada"));

        if (!entrega.getAluno().getId().equals(alunoId)) {
            throw new OperacaoNaoPermitidaException("Acesso negado");
        }

        Avaliacao avaliacao = entrega.getAvaliacao();
        List<Questao> questoes = questaoRepository.findByAvaliacaoIdOrderByOrdem(avaliacao.getId());
        List<Resposta> respostas = respostaRepository.findByEntregaId(entregaId);

        BigDecimal mediaTurma = calcularMediaTurma(avaliacao.getId());
        boolean gabaritoDisponivel = isGabaritoDisponivel(avaliacao);

        Map<Long, Object> respostasMap = respostas.stream()
                .collect(Collectors.toMap(
                        r -> r.getQuestao().getId(),
                        r -> r.getRespostaIndice() != null ? r.getRespostaIndice() : (Object) r.getRespostaTexto()
                ));

        List<GabaritoQuestaoDto> gabarito = gabaritoDisponivel
                ? montarGabarito(entregaId, questoes, respostasMap)
                : List.of();

        List<AnaliseTopicoDto> analise = analisarTopicos(questoes, respostas);

        return new ResultadoEntregaResponse(
                entrega.getNotaFinal(),
                mediaTurma,
                gabaritoDisponivel,
                gabarito,
                analise
        );
    }

    BigDecimal calcularNotaAutomatica(Long entregaId, List<Questao> questoes, Map<Long, Object> respostas) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal obtido = BigDecimal.ZERO;

        for (Questao q : questoes) {
            if (q.getTipo() == TipoQuestaoEnum.DISSERTATIVA || q.getTipo() == TipoQuestaoEnum.UPLOAD_ARQUIVO) {
                continue;
            }
            total = total.add(q.getPontos());
            Object resposta = respostas.get(q.getId());
            if (resposta != null && q.getGabaritoIndice() != null) {
                int indice = toInt(resposta);
                if (indice == q.getGabaritoIndice()) {
                    obtido = obtido.add(q.getPontos());
                }
            }
        }

        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return obtido.multiply(new BigDecimal("10")).divide(total, 2, RoundingMode.HALF_UP);
    }

    private void salvarRespostas(Entrega entrega, Map<Long, Object> respostas, List<Questao> questoes) {
        if (respostas == null) return;
        Map<Long, Questao> questaoMap = questoes.stream()
                .collect(Collectors.toMap(Questao::getId, q -> q));

        for (Map.Entry<Long, Object> entry : respostas.entrySet()) {
            Long questaoId = entry.getKey();
            Questao questao = questaoMap.get(questaoId);
            if (questao == null) continue;

            Resposta resposta = respostaRepository
                    .findByEntregaIdAndQuestaoId(entrega.getId(), questaoId)
                    .orElseGet(() -> {
                        var r = new Resposta();
                        r.setEntrega(entrega);
                        r.setQuestao(questao);
                        return r;
                    });

            preencherResposta(resposta, questao, entry.getValue());
            respostaRepository.save(resposta);
        }
    }

    private void preencherResposta(Resposta resposta, Questao questao, Object valor) {
        if (questao.getTipo() == TipoQuestaoEnum.MULTIPLA_ESCOLHA
                || questao.getTipo() == TipoQuestaoEnum.VERDADEIRO_FALSO) {
            resposta.setRespostaIndice(toInt(valor));
            if (questao.getGabaritoIndice() != null) {
                resposta.setCorreta(toInt(valor) == questao.getGabaritoIndice());
            }
        } else if (questao.getTipo() == TipoQuestaoEnum.DISSERTATIVA) {
            resposta.setRespostaTexto(valor != null ? valor.toString() : null);
        }
    }

    private List<GabaritoQuestaoDto> montarGabarito(Long entregaId, List<Questao> questoes, Map<Long, Object> respostas) {
        return questoes.stream().map(q -> {
            Object respostaAluno = respostas.get(q.getId());
            boolean correta = false;
            BigDecimal pontosObtidos = BigDecimal.ZERO;

            if ((q.getTipo() == TipoQuestaoEnum.MULTIPLA_ESCOLHA || q.getTipo() == TipoQuestaoEnum.VERDADEIRO_FALSO)
                    && respostaAluno != null && q.getGabaritoIndice() != null) {
                correta = toInt(respostaAluno) == q.getGabaritoIndice();
                if (correta) pontosObtidos = q.getPontos();
            }

            return new GabaritoQuestaoDto(
                    q.getId(),
                    q.getGabaritoIndice(),
                    q.getGabaritoDissertativo(),
                    respostaAluno,
                    correta,
                    pontosObtidos,
                    q.getPontos()
            );
        }).toList();
    }

    private List<AnaliseTopicoDto> analisarTopicos(List<Questao> questoes, List<Resposta> respostas) {
        Map<Long, Resposta> respostaMap = respostas.stream()
                .collect(Collectors.toMap(r -> r.getQuestao().getId(), r -> r));

        Map<String, int[]> topicos = new HashMap<>();
        for (Questao q : questoes) {
            if (q.getTopico() == null) continue;
            int[] stats = topicos.computeIfAbsent(q.getTopico(), k -> new int[2]);
            stats[1]++;
            Resposta r = respostaMap.get(q.getId());
            if (r != null && Boolean.TRUE.equals(r.getCorreta())) stats[0]++;
        }

        return topicos.entrySet().stream()
                .map(e -> new AnaliseTopicoDto(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    private BigDecimal calcularMediaTurma(Long avaliacaoId) {
        List<Entrega> entregas = entregaRepository.findAll().stream()
                .filter(e -> e.getAvaliacao().getId().equals(avaliacaoId))
                .filter(e -> e.getNotaFinal() != null)
                .toList();

        if (entregas.isEmpty()) return BigDecimal.ZERO;
        BigDecimal soma = entregas.stream()
                .map(Entrega::getNotaFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(entregas.size()), 2, RoundingMode.HALF_UP);
    }

    private Entrega criarEntrega(Avaliacao avaliacao, Usuario aluno) {
        var e = new Entrega();
        e.setAvaliacao(avaliacao);
        e.setAluno(aluno);
        e.setStatus(StatusEntregaEnum.NAO_INICIADA);
        e.setEntregaAtrasada(false);
        return entregaRepository.save(e);
    }

    private Map<Long, Object> carregarRespostas(Long entregaId) {
        return respostaRepository.findByEntregaId(entregaId).stream()
                .collect(Collectors.toMap(
                        r -> r.getQuestao().getId(),
                        r -> r.getRespostaIndice() != null ? r.getRespostaIndice() : (Object) r.getRespostaTexto()
                ));
    }

    private QuestaoAtividadeDto toQuestaoDto(Questao q) {
        List<String> alternativas = List.of();
        if (q.getAlternativas() != null && !q.getAlternativas().isBlank()) {
            try {
                alternativas = objectMapper.readValue(q.getAlternativas(), new TypeReference<>() {});
            } catch (Exception ignored) {}
        }
        return new QuestaoAtividadeDto(q.getId(), q.getOrdem(), q.getTipo(),
                q.getEnunciado(), alternativas, q.getTopico(), q.getPontos());
    }

    private boolean isGabaritoDisponivel(Avaliacao avaliacao) {
        return avaliacao.getGabaritoLiberacao() == GabaritoLiberacaoEnum.IMEDIATA
                || (avaliacao.getGabaritoLiberacao() == GabaritoLiberacaoEnum.APOS_ENCERRAMENTO
                && avaliacao.getEncerraEm() != null
                && avaliacao.getEncerraEm().isBefore(OffsetDateTime.now()));
    }

    private String resolverStatus(Entrega entrega) {
        if (entrega == null) return "NAO_INICIADO";
        return switch (entrega.getStatus()) {
            case NAO_INICIADA -> "NAO_INICIADO";
            case RASCUNHO -> "EM_ANDAMENTO";
            default -> "ENTREGUE";
        };
    }

    private int toInt(Object valor) {
        if (valor instanceof Integer i) return i;
        if (valor instanceof Number n) return n.intValue();
        try { return Integer.parseInt(valor.toString()); } catch (Exception e) { return -1; }
    }
}
