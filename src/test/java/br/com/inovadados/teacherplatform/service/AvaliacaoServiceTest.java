package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Questao;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import br.com.inovadados.teacherplatform.dto.request.PublicarAvaliacaoRequest;
import br.com.inovadados.teacherplatform.dto.request.QuestaoRascunhoDto;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoRequest;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock TurmaAvaliacaoRepository turmaAvaliacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock ObjectMapper objectMapper;

    @InjectMocks AvaliacaoService avaliacaoService;

    private Turma turma;
    private Usuario professor;
    private UUID professorId;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        turma = new Turma();
        turma.setId(1L);

        professorId = UUID.randomUUID();
        professor = new Usuario();
        professor.setId(professorId);

        avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setTurma(turma);
        avaliacao.setTitulo("Prova 1");
        avaliacao.setStatus(StatusAvaliacaoEnum.RASCUNHO);
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.APOS_ENCERRAMENTO);
    }

    private SalvarRascunhoRequest requisicaoRascunho(List<QuestaoRascunhoDto> questoes) {
        return new SalvarRascunhoRequest("Prova 1", TipoAvaliacaoEnum.PROVA, 1L, 60, questoes);
    }

    private void configurarSalvarRascunhoBase() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> {
            Avaliacao a = inv.getArgument(0);
            if (a.getId() == null) a.setId(10L);
            return a;
        });
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());
    }

    // --- salvarRascunho ---

    @Test
    void salvarRascunho_turmaNaoEncontrada_lancaTurmaNaoEncontradaException() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.empty());
        SalvarRascunhoRequest req = requisicaoRascunho(null);
        assertThrows(TurmaNaoEncontradaException.class, () -> avaliacaoService.salvarRascunho(req, professorId));
    }

    @Test
    void salvarRascunho_professorNaoEncontrado_lancaUnauthorizedException() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.empty());
        SalvarRascunhoRequest req = requisicaoRascunho(null);
        assertThrows(UnauthorizedException.class, () -> avaliacaoService.salvarRascunho(req, professorId));
    }

    @Test
    void salvarRascunho_criaAvaliacaoComStatusRascunho() {
        configurarSalvarRascunhoBase();
        SalvarRascunhoRequest req = requisicaoRascunho(null);

        avaliacaoService.salvarRascunho(req, professorId);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        assertEquals(StatusAvaliacaoEnum.RASCUNHO, captor.getValue().getStatus());
    }

    @Test
    void salvarRascunho_aplicaDefaultsCorretos() {
        configurarSalvarRascunhoBase();
        SalvarRascunhoRequest req = requisicaoRascunho(null);

        avaliacaoService.salvarRascunho(req, professorId);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        Avaliacao a = captor.getValue();
        assertFalse(a.isEmbaralharQuestoes());
        assertFalse(a.isEmbaralharAlternativas());
        assertEquals(GabaritoLiberacaoEnum.APOS_ENCERRAMENTO, a.getGabaritoLiberacao());
        assertFalse(a.isGeradoPorIa());
    }

    @Test
    void salvarRascunho_semQuestoes_naoSalvaQuestoes() {
        configurarSalvarRascunhoBase();
        SalvarRascunhoRequest req = requisicaoRascunho(null);

        avaliacaoService.salvarRascunho(req, professorId);

        verify(questaoRepository, never()).save(any());
    }

    @Test
    void salvarRascunho_comQuestoes_salvaQuestoes() {
        configurarSalvarRascunhoBase();
        QuestaoRascunhoDto dto = new QuestaoRascunhoDto(null, "Questão 1", TipoQuestaoEnum.MULTIPLA_ESCOLHA,
                null, null, 0, null, null, new BigDecimal("2"));
        SalvarRascunhoRequest req = requisicaoRascunho(List.of(dto));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(inv -> inv.getArgument(0));

        avaliacaoService.salvarRascunho(req, professorId);

        verify(questaoRepository, atLeastOnce()).save(any(Questao.class));
    }

    @Test
    void salvarRascunho_ordenacaoDasQuestoes_sequencial() {
        configurarSalvarRascunhoBase();
        QuestaoRascunhoDto q1 = new QuestaoRascunhoDto(null, "Q1", TipoQuestaoEnum.MULTIPLA_ESCOLHA, null, null, 0, null, null, null);
        QuestaoRascunhoDto q2 = new QuestaoRascunhoDto(null, "Q2", TipoQuestaoEnum.DISSERTATIVA, null, null, null, null, null, null);
        SalvarRascunhoRequest req = requisicaoRascunho(List.of(q1, q2));

        List<Integer> ordens = new java.util.ArrayList<>();
        when(questaoRepository.save(any(Questao.class))).thenAnswer(inv -> {
            Questao q = inv.getArgument(0);
            ordens.add(q.getOrdem());
            return q;
        });

        avaliacaoService.salvarRascunho(req, professorId);

        assertEquals(List.of(1, 2), ordens);
    }

    @Test
    void salvarRascunho_questaoSemPontos_recebePontosUm() {
        configurarSalvarRascunhoBase();
        QuestaoRascunhoDto dto = new QuestaoRascunhoDto(null, "Q", TipoQuestaoEnum.DISSERTATIVA, null, null, null, null, null, null);
        SalvarRascunhoRequest req = requisicaoRascunho(List.of(dto));

        ArgumentCaptor<Questao> captor = ArgumentCaptor.forClass(Questao.class);
        when(questaoRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        avaliacaoService.salvarRascunho(req, professorId);

        assertEquals(0, BigDecimal.ONE.compareTo(captor.getValue().getPontos()));
    }

    // --- atualizarRascunho ---

    @Test
    void atualizarRascunho_avaliacaoNaoEncontrada_lancaAvaliacaoNaoEncontradaException() {
        when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());
        SalvarRascunhoRequest req = requisicaoRascunho(null);
        assertThrows(AvaliacaoNaoEncontradaException.class, () -> avaliacaoService.atualizarRascunho(99L, req, professorId));
    }

    @Test
    void atualizarRascunho_statusNaoRascunho_lancaOperacaoNaoPermitida() {
        avaliacao.setStatus(StatusAvaliacaoEnum.PUBLICADA);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        SalvarRascunhoRequest req = requisicaoRascunho(null);
        assertThrows(OperacaoNaoPermitidaException.class, () -> avaliacaoService.atualizarRascunho(1L, req, professorId));
    }

    @Test
    void atualizarRascunho_comQuestoes_deletaERecriia() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);

        Questao qExistente = new Questao();
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of(qExistente));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestaoRascunhoDto dto = new QuestaoRascunhoDto(null, "Nova Q", TipoQuestaoEnum.DISSERTATIVA, null, null, null, null, null, null);
        SalvarRascunhoRequest req = requisicaoRascunho(List.of(dto));

        avaliacaoService.atualizarRascunho(1L, req, professorId);

        verify(questaoRepository).deleteAll(List.of(qExistente));
        verify(questaoRepository, atLeastOnce()).save(any(Questao.class));
    }

    @Test
    void atualizarRascunho_semQuestoes_naoDeleteAll() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());

        SalvarRascunhoRequest req = requisicaoRascunho(null);
        avaliacaoService.atualizarRascunho(1L, req, professorId);

        verify(questaoRepository, never()).deleteAll(any());
    }

    // --- publicar ---

    @Test
    void publicar_statusNaoRascunho_lancaOperacaoNaoPermitida() {
        avaliacao.setStatus(StatusAvaliacaoEnum.PUBLICADA);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        PublicarAvaliacaoRequest req = new PublicarAvaliacaoRequest(
                OffsetDateTime.now().minusHours(1), null, List.of(1L), false, false, GabaritoLiberacaoEnum.IMEDIATA, null);
        assertThrows(OperacaoNaoPermitidaException.class, () -> avaliacaoService.publicar(1L, req));
    }

    @Test
    void publicar_disponivelEmFuturo_statusAgendada() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaAvaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());

        PublicarAvaliacaoRequest req = new PublicarAvaliacaoRequest(
                OffsetDateTime.now().plusHours(1), null, List.of(1L), false, false, GabaritoLiberacaoEnum.IMEDIATA, null);
        avaliacaoService.publicar(1L, req);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        assertEquals(StatusAvaliacaoEnum.AGENDADA, captor.getValue().getStatus());
    }

    @Test
    void publicar_disponivelEmPassado_statusPublicada() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaAvaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());

        PublicarAvaliacaoRequest req = new PublicarAvaliacaoRequest(
                OffsetDateTime.now().minusHours(1), null, List.of(1L), false, false, GabaritoLiberacaoEnum.IMEDIATA, null);
        avaliacaoService.publicar(1L, req);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        assertEquals(StatusAvaliacaoEnum.PUBLICADA, captor.getValue().getStatus());
    }

    @Test
    void publicar_criaTurmaAvaliacaoPorTurma() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
        Turma turma2 = new Turma();
        turma2.setId(2L);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaRepository.findById(2L)).thenReturn(Optional.of(turma2));
        when(turmaAvaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());

        PublicarAvaliacaoRequest req = new PublicarAvaliacaoRequest(
                OffsetDateTime.now().minusHours(1), null, List.of(1L, 2L), false, false, GabaritoLiberacaoEnum.IMEDIATA, null);
        avaliacaoService.publicar(1L, req);

        verify(turmaAvaliacaoRepository, times(2)).save(any());
    }

    @Test
    void publicar_disparaAvaliacaoPublicadaEvent() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaAvaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());

        PublicarAvaliacaoRequest req = new PublicarAvaliacaoRequest(
                OffsetDateTime.now().minusHours(1), null, List.of(1L), false, false, GabaritoLiberacaoEnum.IMEDIATA, null);
        avaliacaoService.publicar(1L, req);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertInstanceOf(AvaliacaoPublicadaEvent.class, captor.getValue());
    }

    @Test
    void publicar_pesoNotaAtualizado() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaAvaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(anyLong())).thenReturn(List.of());

        PublicarAvaliacaoRequest req = new PublicarAvaliacaoRequest(
                OffsetDateTime.now().minusHours(1), null, List.of(1L), false, false, GabaritoLiberacaoEnum.IMEDIATA, new BigDecimal("3.5"));
        avaliacaoService.publicar(1L, req);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("3.5").compareTo(captor.getValue().getPesoNota()));
    }

    // --- preview ---

    @Test
    void preview_avaliacaoNaoEncontrada_lancaAvaliacaoNaoEncontradaException() {
        when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AvaliacaoNaoEncontradaException.class, () -> avaliacaoService.preview(99L, UUID.randomUUID()));
    }

    @Test
    void preview_semEmbaralhamento_questoesNaOrdemOriginal() {
        avaliacao.setEmbaralharQuestoes(false);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        Questao q1 = new Questao(); q1.setId(1L); q1.setOrdem(1); q1.setPontos(BigDecimal.ONE);
        Questao q2 = new Questao(); q2.setId(2L); q2.setOrdem(2); q2.setPontos(BigDecimal.ONE);
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of(q1, q2));

        var resp = avaliacaoService.preview(1L, UUID.randomUUID());

        assertEquals(2, resp.questoes().size());
        assertEquals(1, resp.questoes().get(0).ordem());
        assertEquals(2, resp.questoes().get(1).ordem());
    }

    @Test
    void preview_comEmbaralhamento_mesmaSeedMesmaOrdem() {
        avaliacao.setEmbaralharQuestoes(true);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        Questao q1 = new Questao(); q1.setId(1L); q1.setOrdem(1); q1.setPontos(BigDecimal.ONE);
        Questao q2 = new Questao(); q2.setId(2L); q2.setOrdem(2); q2.setPontos(BigDecimal.ONE);
        Questao q3 = new Questao(); q3.setId(3L); q3.setOrdem(3); q3.setPontos(BigDecimal.ONE);
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of(q1, q2, q3));

        UUID seed = UUID.randomUUID();
        var resp1 = avaliacaoService.preview(1L, seed);
        var resp2 = avaliacaoService.preview(1L, seed);

        List<Integer> ordens1 = resp1.questoes().stream().map(q -> q.ordem()).toList();
        List<Integer> ordens2 = resp2.questoes().stream().map(q -> q.ordem()).toList();
        assertEquals(ordens1, ordens2);
    }

    @Test
    void preview_gabaritoNaoExpostoNasQuestoes() {
        avaliacao.setEmbaralharQuestoes(false);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        Questao q = new Questao();
        q.setId(1L);
        q.setOrdem(1);
        q.setGabaritoIndice(2);
        q.setGabaritoDissertativo("Gabarito secreto");
        q.setPontos(BigDecimal.ONE);
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of(q));

        var resp = avaliacaoService.preview(1L, UUID.randomUUID());

        assertNull(resp.questoes().get(0).gabaritoIndice());
        assertNull(resp.questoes().get(0).gabaritoDissertativo());
    }
}
