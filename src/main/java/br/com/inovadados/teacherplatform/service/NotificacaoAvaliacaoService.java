package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.PreferenciasNotificacao;
import br.com.inovadados.teacherplatform.domain.entity.VinculoResponsavel;
import br.com.inovadados.teacherplatform.event.AvaliacaoPublicadaEvent;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.PreferenciasNotificacaoRepository;
import br.com.inovadados.teacherplatform.repository.VinculoResponsavelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoAvaliacaoService {

    private final MatriculaRepository matriculaRepository;
    private final VinculoResponsavelRepository vinculoRepository;
    private final PreferenciasNotificacaoRepository preferenciasRepository;
    private final PushNotificationService pushNotificationService;

    @Async
    @EventListener
    public void onAvaliacaoPublicada(AvaliacaoPublicadaEvent event) {
        log.info("Avaliação {} publicada para {} turmas — disparando notificações",
                event.avaliacaoId(), event.turmasIds().size());

        event.turmasIds().forEach(turmaId ->
            matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId).forEach(matricula -> {
                UUID alunoId = matricula.getAluno().getId();

                // Notificar o aluno diretamente
                notificarSePermitido(alunoId,
                        "Nova avaliação disponível",
                        "Uma nova avaliação foi publicada. Acesse o app para ver detalhes.",
                        "/aluno/feed",
                        "prazoProva");

                // Notificar os responsáveis do aluno
                vinculoRepository.findByAlunoId(alunoId).stream()
                        .map(VinculoResponsavel::getResponsavel)
                        .forEach(responsavel -> notificarSePermitido(
                                responsavel.getId(),
                                "Nova prova agendada",
                                "Seu filho tem uma nova avaliação. Acesse o app para acompanhar.",
                                "/responsavel/acompanhamento?tab=calendario",
                                "prazoProva"));
            })
        );
    }

    private void notificarSePermitido(UUID usuarioId, String titulo, String corpo, String url, String prefChave) {
        PreferenciasNotificacao prefs = preferenciasRepository.findByUsuarioId(usuarioId).orElse(null);
        boolean permitido = prefs == null || switch (prefChave) {
            case "prazoProva" -> prefs.isPrazoProva();
            case "faltaAluno" -> prefs.isFaltaAluno();
            case "quedaFrequencia" -> prefs.isQuedaFrequencia();
            default -> true;
        };
        if (permitido) {
            pushNotificationService.enviarParaUsuario(usuarioId, titulo, corpo, url);
        }
    }
}
