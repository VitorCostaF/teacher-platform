package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Conquista;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.FlashcardEstadoSm2;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.repository.ConquistaRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.FlashcardEstadoSm2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificacaoServiceTest {

    @Mock EntregaRepository entregaRepository;
    @Mock FlashcardEstadoSm2Repository estadoSm2Repository;
    @Mock ConquistaRepository conquistaRepository;

    @InjectMocks GamificacaoService gamificacaoService;

    private UUID alunoId;

    @BeforeEach
    void setUp() {
        alunoId = UUID.randomUUID();
    }

    private Entrega entregaFake(StatusEntregaEnum status, boolean atrasada, String nota) {
        Entrega e = new Entrega();
        e.setStatus(status);
        e.setEntregaAtrasada(atrasada);
        e.setNotaFinal(nota != null ? new BigDecimal(nota) : null);
        return e;
    }

    private FlashcardEstadoSm2 estadoComRevisoes(int total) {
        FlashcardEstadoSm2 e = new FlashcardEstadoSm2();
        e.setTotalRevisoes(total);
        return e;
    }

    private void mockEntregas(List<Entrega> entregas) {
        when(entregaRepository.findByAlunoId(alunoId)).thenReturn(entregas);
    }

    private void mockFlashcards(List<FlashcardEstadoSm2> estados) {
        when(estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                eq(alunoId), any(LocalDate.class)))
            .thenReturn(estados);
    }

    // --- calcularPontos ---

    @Test
    void calcularPontos_semEntregasNemFlashcards_deveRetornarZero() {
        mockEntregas(List.of());
        mockFlashcards(List.of());

        assertEquals(0, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_1EntregaNoPrazoSemNota_deveRetornar10() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.ENTREGUE, false, null)));
        mockFlashcards(List.of());

        assertEquals(10, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_1EntregaNoPrazoNotaOito_deveRetornar30() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.ENTREGUE, false, "8.0")));
        mockFlashcards(List.of());

        assertEquals(30, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_1EntregaAtrasadaSemNota_deveRetornar3() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.ENTREGUE, true, null)));
        mockFlashcards(List.of());

        assertEquals(3, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_1EntregaAtrasadaNotaNove_deveRetornar23() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.ENTREGUE, true, "9.0")));
        mockFlashcards(List.of());

        assertEquals(23, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_notaMenorQue7_semBonus() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.ENTREGUE, false, "6.9")));
        mockFlashcards(List.of());

        assertEquals(10, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_notaExatamente7_incluiBonus() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.ENTREGUE, false, "7.0")));
        mockFlashcards(List.of());

        assertEquals(30, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_entregaRascunhoComNotaAlta_deveRetornarZero() {
        mockEntregas(List.of(entregaFake(StatusEntregaEnum.RASCUNHO, false, "10.0")));
        mockFlashcards(List.of());

        assertEquals(0, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_5RevisoesDeFlashcard_deveRetornar5() {
        mockEntregas(List.of());
        mockFlashcards(List.of(estadoComRevisoes(3), estadoComRevisoes(2)));

        assertEquals(5, gamificacaoService.calcularPontos(alunoId));
    }

    @Test
    void calcularPontos_mixEntregasEFlashcards_somacorreto() {
        mockEntregas(List.of(
            entregaFake(StatusEntregaEnum.ENTREGUE, false, "8.0"),
            entregaFake(StatusEntregaEnum.ENTREGUE, true, null)
        ));
        mockFlashcards(List.of(estadoComRevisoes(5)));

        assertEquals(38, gamificacaoService.calcularPontos(alunoId));
    }

    // --- verificarConquistas: DEDICADO ---

    @Test
    void verificarConquistas_dedicado_criaDurante10EntregasNoPrazo() {
        List<Entrega> entregas = Collections.nCopies(10, entregaFake(StatusEntregaEnum.ENTREGUE, false, null));
        mockEntregas(entregas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(false);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        ArgumentCaptor<Conquista> captor = ArgumentCaptor.forClass(Conquista.class);
        verify(conquistaRepository, times(1)).save(captor.capture());
        assertEquals("DEDICADO", captor.getValue().getTipo());
    }

    @Test
    void verificarConquistas_dedicado_naoReplicaSeJaExiste() {
        List<Entrega> entregas = Collections.nCopies(10, entregaFake(StatusEntregaEnum.ENTREGUE, false, null));
        mockEntregas(entregas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        verify(conquistaRepository, never()).save(any());
    }

    @Test
    void verificarConquistas_dedicado_naoSalvaComMenosDe10() {
        List<Entrega> entregas = Collections.nCopies(9, entregaFake(StatusEntregaEnum.ENTREGUE, false, null));
        mockEntregas(entregas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        verify(conquistaRepository, never()).save(any());
    }

    @Test
    void verificarConquistas_dedicado_naoContaEntregasAtrasadas() {
        List<Entrega> atrasadas = Collections.nCopies(10, entregaFake(StatusEntregaEnum.ENTREGUE, true, null));
        mockEntregas(atrasadas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        verify(conquistaRepository, never()).save(any());
    }

    // --- verificarConquistas: EXCELENCIA ---

    @Test
    void verificarConquistas_excelencia_criaQuando5NotasAcimaDe9() {
        List<Entrega> entregas = Collections.nCopies(5, entregaFake(StatusEntregaEnum.ENTREGUE, false, "9.0"));
        mockEntregas(entregas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(false);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        ArgumentCaptor<Conquista> captor = ArgumentCaptor.forClass(Conquista.class);
        verify(conquistaRepository, times(1)).save(captor.capture());
        assertEquals("EXCELENCIA", captor.getValue().getTipo());
    }

    @Test
    void verificarConquistas_excelencia_nota89NaoContabiliza() {
        List<Entrega> entregas = Collections.nCopies(5, entregaFake(StatusEntregaEnum.ENTREGUE, false, "8.9"));
        mockEntregas(entregas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        verify(conquistaRepository, never()).save(any());
    }

    @Test
    void verificarConquistas_excelencia_entregaSemNotaNaoContabiliza() {
        List<Entrega> entregas = Collections.nCopies(5, entregaFake(StatusEntregaEnum.ENTREGUE, false, null));
        mockEntregas(entregas);
        mockFlashcards(List.of());
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        verify(conquistaRepository, never()).save(any());
    }

    // --- verificarConquistas: FLASHMASTER ---

    @Test
    void verificarConquistas_flashmaster_criaQuandoSomaAcimaDe50() {
        mockEntregas(List.of());
        mockFlashcards(List.of(estadoComRevisoes(30), estadoComRevisoes(25)));
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(false);

        gamificacaoService.verificarConquistas(alunoId);

        ArgumentCaptor<Conquista> captor = ArgumentCaptor.forClass(Conquista.class);
        verify(conquistaRepository, times(1)).save(captor.capture());
        assertEquals("FLASHMASTER", captor.getValue().getTipo());
    }

    @Test
    void verificarConquistas_flashmaster_naoSalvaComMenosDe50() {
        mockEntregas(List.of());
        mockFlashcards(List.of(estadoComRevisoes(49)));
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);

        gamificacaoService.verificarConquistas(alunoId);

        verify(conquistaRepository, never()).save(any());
    }

    @Test
    void verificarConquistas_conquistaSalvaComCamposCorretos() {
        mockEntregas(List.of());
        mockFlashcards(List.of(estadoComRevisoes(50)));
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")).thenReturn(true);
        when(conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")).thenReturn(false);

        gamificacaoService.verificarConquistas(alunoId);

        ArgumentCaptor<Conquista> captor = ArgumentCaptor.forClass(Conquista.class);
        verify(conquistaRepository).save(captor.capture());
        Conquista c = captor.getValue();
        assertEquals("FLASHMASTER", c.getTipo());
        assertNotNull(c.getDescricao());
        assertNotNull(c.getObtidaEm());
    }
}
