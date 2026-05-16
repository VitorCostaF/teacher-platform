package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.PreferenciasNotificacao;
import br.com.inovadados.teacherplatform.domain.entity.PushSubscription;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.repository.PreferenciasNotificacaoRepository;
import br.com.inovadados.teacherplatform.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaFrequenciaServiceTest {

    @Mock PushNotificationService pushNotificationService;
    @Mock PreferenciasNotificacaoRepository preferenciasRepository;
    @Mock PushSubscriptionRepository pushSubscriptionRepository;

    @InjectMocks AlertaFrequenciaService alertaFrequenciaService;

    private UUID alunoId;
    private UUID responsavelId;
    private Long turmaId;

    @BeforeEach
    void setUp() {
        alunoId = UUID.randomUUID();
        responsavelId = UUID.randomUUID();
        turmaId = 1L;
    }

    private PushSubscription subFake(Long id, UUID respId) {
        PushSubscription sub = new PushSubscription();
        sub.setId(id);
        Usuario usuario = new Usuario();
        usuario.setId(respId);
        sub.setUsuario(usuario);
        return sub;
    }

    private PreferenciasNotificacao prefs(boolean faltaAluno, boolean quedaFrequencia) {
        PreferenciasNotificacao p = new PreferenciasNotificacao();
        p.setFaltaAluno(faltaAluno);
        p.setQuedaFrequencia(quedaFrequencia);
        return p;
    }

    // --- critério de frequência ---

    @Test
    void verificarAlertas_percentualAbaixoDe75_enviaAlerta() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId)).thenReturn(Optional.empty());

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 70.0, false);

        verify(pushNotificationService, times(1)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_percentualIgual75_naoEnvia() {
        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 75.0, false);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_percentualAcimaDe75_naoEnvia() {
        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 90.0, false);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_percentualZero_enviaAlerta() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId)).thenReturn(Optional.empty());

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 0.0, false);

        verify(pushNotificationService, times(1)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    // --- critério de faltas consecutivas ---

    @Test
    void verificarAlertas_tresFaltasConsecutivas_enviaAlerta() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId)).thenReturn(Optional.empty());

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 80.0, true);

        verify(pushNotificationService, times(1)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_tresFaltasFalse_naoEnvia() {
        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 80.0, false);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    // --- ambos critérios ---

    @Test
    void verificarAlertas_ambosAtivos_enviaDuasVezesPorResponsavel() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId)).thenReturn(Optional.empty());

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 50.0, true);

        // frequência envia 1x + consecutivas envia 1x = 2 chamadas totais para mesma sub
        verify(pushNotificationService, times(2)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    // --- sem subscriptions ---

    @Test
    void verificarAlertas_semSubscriptions_naoEnviaEnSemExcecao() {
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of());

        assertDoesNotThrow(() -> alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 50.0, true));
        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    // --- preferências do responsável ---

    @Test
    void verificarAlertas_semPreferencias_enviaComPadraoHabilitado() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId)).thenReturn(Optional.empty());

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 60.0, true);

        verify(pushNotificationService, times(2)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_faltaAlunoTrue_enviaPorConsecutivas() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId))
                .thenReturn(Optional.of(prefs(true, true)));

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 80.0, true);

        verify(pushNotificationService, times(1)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_faltaAlunoFalse_naoEnviaPorConsecutivas() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId))
                .thenReturn(Optional.of(prefs(false, true)));

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 80.0, true);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_quedaFrequenciaTrue_enviaPorFrequencia() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId))
                .thenReturn(Optional.of(prefs(true, true)));

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 60.0, false);

        verify(pushNotificationService, times(1)).enviar(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_quedaFrequenciaFalse_naoEnviaPorFrequencia() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId))
                .thenReturn(Optional.of(prefs(true, false)));

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 60.0, false);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    // --- iteração por responsável ---

    @Test
    void verificarAlertas_doisResponsaveis_enviaParaCada() {
        UUID resp2 = UUID.randomUUID();
        PushSubscription sub1 = subFake(1L, responsavelId);
        PushSubscription sub2 = subFake(2L, resp2);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub1, sub2));
        when(preferenciasRepository.findByUsuarioId(responsavelId)).thenReturn(Optional.empty());
        when(preferenciasRepository.findByUsuarioId(resp2)).thenReturn(Optional.empty());

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 60.0, false);

        verify(pushNotificationService, times(1)).enviar(eq(1L), anyString(), anyString(), anyString());
        verify(pushNotificationService, times(1)).enviar(eq(2L), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_percentualBaixoPreferenciaDesabilitada_naoEnvia() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId))
                .thenReturn(Optional.of(prefs(true, false))); // quedaFrequencia=false

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 50.0, false);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

    @Test
    void verificarAlertas_consecutivasPreferenciasDesabilitada_naoEnvia() {
        PushSubscription sub = subFake(1L, responsavelId);
        when(pushSubscriptionRepository.findAllByUsuarioId(alunoId)).thenReturn(List.of(sub));
        when(preferenciasRepository.findByUsuarioId(responsavelId))
                .thenReturn(Optional.of(prefs(false, true))); // faltaAluno=false

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, 80.0, true);

        verify(pushNotificationService, never()).enviar(any(), anyString(), anyString(), anyString());
    }

}
