package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Questao;
import br.com.inovadados.teacherplatform.domain.entity.TurmaAvaliacao;
import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.dto.request.PublicarAvaliacaoRequest;
import br.com.inovadados.teacherplatform.dto.request.QuestaoRascunhoDto;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoRequest;
import br.com.inovadados.teacherplatform.dto.response.AvaliacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.PreviewAvaliacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.QuestaoAvaliacaoDto;
import br.com.inovadados.teacherplatform.event.AvaliacaoPublicadaEvent;
import br.com.inovadados.teacherplatform.exception.AvaliacaoNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.OperacaoNaoPermitidaException;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.QuestaoRepository;
import br.com.inovadados.teacherplatform.repository.TurmaAvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoRepository questaoRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaAvaliacaoRepository turmaAvaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public AvaliacaoResponse salvarRascunho(SalvarRascunhoRequest req, UUID professorId) {
        var turma = turmaRepository.findById(req.turmaId())
                .orElseThrow(() -> new TurmaNaoEncontradaException(req.turmaId()));
        var professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        var avaliacao = new Avaliacao();
        avaliacao.setTurma(turma);
        avaliacao.setProfessor(professor);
        avaliacao.setTitulo(req.titulo());
        avaliacao.setTipo(req.tipo());
        avaliacao.setStatus(StatusAvaliacaoEnum.RASCUNHO);
        avaliacao.setDuracaoMinutos(req.duracaoMinutos());
        avaliacao.setEmbaralharQuestoes(false);
        avaliacao.setEmbaralharAlternativas(false);
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.APOS_ENCERRAMENTO);
        avaliacao.setGeradoPorIa(false);
        avaliacao.setCriadoEm(OffsetDateTime.now());
        avaliacao = avaliacaoRepository.save(avaliacao);

        if (req.questoes() != null) {
            salvarQuestoes(avaliacao, req.questoes());
        }

        return toAvaliacaoResponse(avaliacao);
    }

    public AvaliacaoResponse atualizarRascunho(Long id, SalvarRascunhoRequest req, UUID professorId) {
        var avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(id));

        if (avaliacao.getStatus() != StatusAvaliacaoEnum.RASCUNHO) {
            throw new OperacaoNaoPermitidaException("Apenas avaliações em rascunho podem ser editadas");
        }

        var turma = turmaRepository.findById(req.turmaId())
                .orElseThrow(() -> new TurmaNaoEncontradaException(req.turmaId()));

        avaliacao.setTurma(turma);
        avaliacao.setTitulo(req.titulo());
        avaliacao.setTipo(req.tipo());
        avaliacao.setDuracaoMinutos(req.duracaoMinutos());

        if (req.questoes() != null) {
            questaoRepository.deleteAll(questaoRepository.findByAvaliacaoIdOrderByOrdem(id));
            salvarQuestoes(avaliacao, req.questoes());
        }

        avaliacao = avaliacaoRepository.save(avaliacao);
        return toAvaliacaoResponse(avaliacao);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarAvaliacao(Long id) {
        var avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(id));
        return toAvaliacaoResponse(avaliacao);
    }

    @Transactional(readOnly = true)
    public PreviewAvaliacaoResponse preview(Long id, UUID seed) {
        var avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(id));

        List<QuestaoAvaliacaoDto> questoes = toQuestoesDtos(
                questaoRepository.findByAvaliacaoIdOrderByOrdem(id), false);

        if (avaliacao.isEmbaralharQuestoes()) {
            questoes = new ArrayList<>(questoes);
            Collections.shuffle(questoes, new Random(
                    seed.getMostSignificantBits() ^ seed.getLeastSignificantBits()));
        }

        return new PreviewAvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getTitulo(),
                avaliacao.getTipo(),
                avaliacao.getDuracaoMinutos(),
                avaliacao.getDisponivelEm(),
                avaliacao.getPesoNota(),
                questoes
        );
    }

    public AvaliacaoResponse publicar(Long id, PublicarAvaliacaoRequest req) {
        var avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException(id));

        if (avaliacao.getStatus() != StatusAvaliacaoEnum.RASCUNHO) {
            throw new OperacaoNaoPermitidaException("Apenas avaliações em rascunho podem ser publicadas");
        }

        avaliacao.setDisponivelEm(req.disponivelEm());
        avaliacao.setEncerraEm(req.encerraEm());
        avaliacao.setEmbaralharQuestoes(req.embaralharQuestoes());
        avaliacao.setEmbaralharAlternativas(req.embaralharAlternativas());
        avaliacao.setGabaritoLiberacao(req.gabaritoLiberacao());
        if (req.pesoNota() != null) avaliacao.setPesoNota(req.pesoNota());

        boolean agendada = req.disponivelEm().isAfter(OffsetDateTime.now());
        avaliacao.setStatus(agendada ? StatusAvaliacaoEnum.AGENDADA : StatusAvaliacaoEnum.PUBLICADA);
        avaliacao = avaliacaoRepository.save(avaliacao);

        for (Long turmaId : req.turmasIds()) {
            var turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));
            var ta = new TurmaAvaliacao();
            ta.setAvaliacao(avaliacao);
            ta.setTurma(turma);
            ta.setPublicadoEm(OffsetDateTime.now());
            turmaAvaliacaoRepository.save(ta);
        }

        eventPublisher.publishEvent(new AvaliacaoPublicadaEvent(avaliacao.getId(), req.turmasIds()));
        return toAvaliacaoResponse(avaliacao);
    }

    private void salvarQuestoes(Avaliacao avaliacao, List<QuestaoRascunhoDto> dtos) {
        int ordem = 1;
        for (QuestaoRascunhoDto dto : dtos) {
            var q = new Questao();
            q.setAvaliacao(avaliacao);
            q.setOrdem(ordem++);
            q.setTipo(dto.tipo());
            q.setEnunciado(dto.enunciado());
            q.setGabaritoDissertativo(dto.gabaritoDissertativo());
            q.setGabaritoIndice(dto.gabaritoIndice());
            q.setDificuldade(dto.dificuldade());
            q.setTopico(dto.topico());
            q.setPontos(dto.pontos() != null ? dto.pontos() : BigDecimal.ONE);
            if (dto.alternativas() != null && !dto.alternativas().isEmpty()) {
                q.setAlternativas(serializarAlternativas(dto.alternativas()));
            }
            questaoRepository.save(q);
        }
    }

    private String serializarAlternativas(List<String> alternativas) {
        try {
            return objectMapper.writeValueAsString(alternativas);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> deserializarAlternativas(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private AvaliacaoResponse toAvaliacaoResponse(Avaliacao avaliacao) {
        List<Questao> questoes = questaoRepository.findByAvaliacaoIdOrderByOrdem(avaliacao.getId());
        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getTitulo(),
                avaliacao.getTipo(),
                avaliacao.getStatus(),
                avaliacao.getDuracaoMinutos(),
                avaliacao.getDisponivelEm(),
                avaliacao.getEncerraEm(),
                avaliacao.isEmbaralharQuestoes(),
                avaliacao.isEmbaralharAlternativas(),
                avaliacao.getGabaritoLiberacao(),
                avaliacao.getPesoNota(),
                avaliacao.isGeradoPorIa(),
                avaliacao.getCriadoEm(),
                toQuestoesDtos(questoes, true)
        );
    }

    private List<QuestaoAvaliacaoDto> toQuestoesDtos(List<Questao> questoes, boolean incluirGabarito) {
        return questoes.stream()
                .map(q -> new QuestaoAvaliacaoDto(
                        q.getId(),
                        q.getOrdem(),
                        q.getTipo(),
                        q.getEnunciado(),
                        deserializarAlternativas(q.getAlternativas()),
                        q.getDificuldade(),
                        q.getTopico(),
                        q.getPontos(),
                        incluirGabarito ? q.getGabaritoDissertativo() : null,
                        incluirGabarito ? q.getGabaritoIndice() : null
                ))
                .collect(Collectors.toList());
    }
}
