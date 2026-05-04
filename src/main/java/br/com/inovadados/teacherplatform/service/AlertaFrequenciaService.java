package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.PreferenciasNotificacao;
import br.com.inovadados.teacherplatform.repository.PreferenciasNotificacaoRepository;
import br.com.inovadados.teacherplatform.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaFrequenciaService {

    private final PushNotificationService pushNotificationService;
    private final PreferenciasNotificacaoRepository preferenciasRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Async
    public void verificarAlertas(Long turmaId, UUID alunoId, double percentual, boolean tresFaltasConsecutivas) {
        if (percentual < 75.0) {
            log.info("Alerta frequência: aluno {} turma {} — {}% de presença", alunoId, turmaId, String.format("%.1f", percentual));
            notificarResponsaveis(alunoId,
                    "Frequência abaixo do mínimo",
                    String.format("A frequência do seu filho está em %.1f%%, abaixo dos 75%% exigidos.", percentual),
                    "/responsavel/acompanhamento?tab=frequencia",
                    "quedaFrequencia");
        }

        if (tresFaltasConsecutivas) {
            log.info("Alerta frequência: aluno {} turma {} — 3 faltas consecutivas", alunoId, turmaId);
            notificarResponsaveis(alunoId,
                    "Faltas consecutivas registradas",
                    "Seu filho registrou 3 faltas consecutivas. Verifique o calendário de frequência.",
                    "/responsavel/acompanhamento?tab=frequencia",
                    "faltaAluno");
        }
    }

    private void notificarResponsaveis(UUID alunoId, String titulo, String corpo, String url, String prefChave) {
        // Buscar responsáveis do aluno via push_subscriptions
        // A busca é por responsáveis vinculados — o serviço de push itera sobre as subscriptions do usuário
        pushSubscriptionRepository.findAllByUsuarioId(alunoId).forEach(sub -> {
            UUID responsavelId = sub.getUsuario().getId();
            PreferenciasNotificacao prefs = preferenciasRepository.findByUsuarioId(responsavelId).orElse(null);
            boolean permitido = prefs == null || switch (prefChave) {
                case "faltaAluno" -> prefs.isFaltaAluno();
                case "quedaFrequencia" -> prefs.isQuedaFrequencia();
                default -> true;
            };
            if (permitido) {
                pushNotificationService.enviar(sub.getId(), titulo, corpo, url);
            }
        });
    }
}
