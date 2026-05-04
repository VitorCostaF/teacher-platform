package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Questao;
import br.com.inovadados.teacherplatform.domain.entity.SessaoProva;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.domain.entity.Resposta;
import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import br.com.inovadados.teacherplatform.dto.request.AutosaveProvaRequest;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoAtividadeRequest;
import br.com.inovadados.teacherplatform.dto.response.EntregarAtividadeResponse;
import br.com.inovadados.teacherplatform.dto.response.SessaoProvaResponse;
import br.com.inovadados.teacherplatform.exception.AvaliacaoNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.OperacaoNaoPermitidaException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.QuestaoRepository;
import br.com.inovadados.teacherplatform.repository.RespostaRepository;
import br.com.inovadados.teacherplatform.repository.SessaoProvaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SessaoProvaService {

    private static final Logger log = LoggerFactory.getLogger(SessaoProvaService.class);

    private final SessaoProvaRepository sessaoProvaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EntregaRepository entregaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AtividadeService atividadeService;

    public SessaoProvaResponse iniciar(Long provaId, UUID alunoId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(provaId)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(provaId));

        if (avaliacao.getStatus() != StatusAvaliacaoEnum.PUBLICADA) {
            throw new OperacaoNaoPermitidaException("Prova não disponível");
        }

        OffsetDateTime agora = OffsetDateTime.now();
        if (avaliacao.getDisponivelEm() != null && avaliacao.getDisponivelEm().isAfter(agora)) {
            throw new OperacaoNaoPermitidaException("Prova ainda não disponível");
        }
        if (avaliacao.getEncerraEm() != null && avaliacao.getEncerraEm().isBefore(agora)) {
            throw new OperacaoNaoPermitidaException("Prova encerrada");
        }

        SessaoProva sessao = sessaoProvaRepository
                .findByAvaliacaoIdAndAlunoId(provaId, alunoId)
                .orElseGet(() -> criarSessao(avaliacao, alunoId));

        if (sessao.getEncerradaEm() != null) {
            throw new OperacaoNaoPermitidaException("Sessão já encerrada");
        }

        Map<Long, Object> respostasParciais = carregarRespostasParciais(provaId, alunoId);
        int duracao = avaliacao.getDuracaoMinutos() != null ? avaliacao.getDuracaoMinutos() : 0;

        return new SessaoProvaResponse(sessao.getId(), sessao.getIniciadaEm(), duracao, respostasParciais);
    }

    public void autosave(Long sessaoId, AutosaveProvaRequest req, UUID alunoId) {
        SessaoProva sessao = sessaoProvaRepository.findById(sessaoId)
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Sessão não encontrada"));

        validarSessaoAtiva(sessao, alunoId);

        if (req.eventoVisibilidade() != null) {
            log.info("Evento de visibilidade — sessao={} evento={}", sessaoId, req.eventoVisibilidade());
        }

        Long provaId = sessao.getAvaliacao().getId();
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
        Avaliacao avaliacao = sessao.getAvaliacao();

        Entrega entrega = entregaRepository.findByAvaliacaoIdAndAlunoId(provaId, alunoId)
                .orElseGet(() -> criarEntrega(avaliacao, aluno));

        if (entrega.getStatus() == StatusEntregaEnum.NAO_INICIADA) {
            entrega.setStatus(StatusEntregaEnum.RASCUNHO);
            entrega.setIniciadoEm(sessao.getIniciadaEm());
            entregaRepository.save(entrega);
        }

        List<Questao> questoes = questaoRepository.findByAvaliacaoIdOrderByOrdem(provaId);
        salvarRespostasParciais(entrega, req.respostas(), questoes);
    }

    public EntregarAtividadeResponse entregar(Long provaId, Long sessaoId, UUID alunoId) {
        SessaoProva sessao = sessaoProvaRepository.findById(sessaoId)
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Sessão não encontrada"));

        validarSessaoAtiva(sessao, alunoId);

        Map<Long, Object> respostas = carregarRespostasParciais(provaId, alunoId);
        EntregarAtividadeResponse resultado = atividadeService.entregar(
                provaId,
                new SalvarRascunhoAtividadeRequest(respostas),
                alunoId
        );

        sessao.setEncerradaEm(OffsetDateTime.now());
        sessao.setEntregueManualmente(true);
        sessaoProvaRepository.save(sessao);

        return resultado;
    }

    public void encerrarPorExpiracao(SessaoProva sessao) {
        Long provaId = sessao.getAvaliacao().getId();
        UUID alunoId = sessao.getAluno().getId();

        try {
            Map<Long, Object> respostas = carregarRespostasParciais(provaId, alunoId);
            atividadeService.entregar(provaId, new SalvarRascunhoAtividadeRequest(respostas), alunoId);
        } catch (OperacaoNaoPermitidaException e) {
            log.warn("Sessão {} já entregue, ignorando expiração", sessao.getId());
        } catch (Exception e) {
            log.error("Erro ao expirar sessão {}: {}", sessao.getId(), e.getMessage());
        }

        sessao.setEncerradaEm(OffsetDateTime.now());
        sessao.setEntregueManualmente(false);
        sessaoProvaRepository.save(sessao);
    }

    long calcularTempoRestanteSegundos(SessaoProva sessao) {
        if (sessao.getAvaliacao().getDuracaoMinutos() == null) return Long.MAX_VALUE;
        OffsetDateTime expiracao = sessao.getIniciadaEm()
                .plusMinutes(sessao.getAvaliacao().getDuracaoMinutos());
        long segundos = java.time.Duration.between(OffsetDateTime.now(), expiracao).getSeconds();
        return Math.max(0, segundos);
    }

    private SessaoProva criarSessao(Avaliacao avaliacao, UUID alunoId) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
        var sessao = new SessaoProva();
        sessao.setAvaliacao(avaliacao);
        sessao.setAluno(aluno);
        sessao.setIniciadaEm(OffsetDateTime.now());
        sessao.setEntregueManualmente(false);
        return sessaoProvaRepository.save(sessao);
    }

    private Entrega criarEntrega(Avaliacao avaliacao, Usuario aluno) {
        var e = new Entrega();
        e.setAvaliacao(avaliacao);
        e.setAluno(aluno);
        e.setStatus(StatusEntregaEnum.NAO_INICIADA);
        e.setEntregaAtrasada(false);
        return entregaRepository.save(e);
    }

    private void validarSessaoAtiva(SessaoProva sessao, UUID alunoId) {
        if (!sessao.getAluno().getId().equals(alunoId)) {
            throw new OperacaoNaoPermitidaException("Acesso negado");
        }
        if (sessao.getEncerradaEm() != null) {
            throw new OperacaoNaoPermitidaException("Sessão já encerrada");
        }
    }

    private Map<Long, Object> carregarRespostasParciais(Long provaId, UUID alunoId) {
        return entregaRepository.findByAvaliacaoIdAndAlunoId(provaId, alunoId)
                .map(entrega -> respostaRepository.findByEntregaId(entrega.getId()).stream()
                        .filter(r -> r.getRespostaIndice() != null || r.getRespostaTexto() != null)
                        .collect(Collectors.toMap(
                                r -> r.getQuestao().getId(),
                                r -> r.getRespostaIndice() != null
                                        ? (Object) r.getRespostaIndice()
                                        : r.getRespostaTexto()
                        )))
                .orElse(Map.of());
    }

    private void salvarRespostasParciais(Entrega entrega, Map<Long, Object> respostas,
                                         List<Questao> questoes) {
        if (respostas == null) return;
        Map<Long, Questao> questaoMap = questoes.stream()
                .collect(Collectors.toMap(Questao::getId, q -> q));

        for (Map.Entry<Long, Object> entry : respostas.entrySet()) {
            Questao questao = questaoMap.get(entry.getKey());
            if (questao == null) continue;

            Resposta resposta = respostaRepository.findByEntregaIdAndQuestaoId(entrega.getId(), entry.getKey())
                    .orElseGet(() -> {
                        var r = new Resposta();
                        r.setEntrega(entrega);
                        r.setQuestao(questao);
                        return r;
                    });

            Object valor = entry.getValue();
            if (questao.getTipo() == TipoQuestaoEnum.DISSERTATIVA) {
                resposta.setRespostaTexto(valor != null ? valor.toString() : null);
            } else {
                resposta.setRespostaIndice(toInt(valor));
            }
            respostaRepository.save(resposta);
        }
    }

    private int toInt(Object valor) {
        if (valor instanceof Integer i) return i;
        if (valor instanceof Number n) return n.intValue();
        try { return Integer.parseInt(valor.toString()); } catch (Exception e) { return -1; }
    }
}
