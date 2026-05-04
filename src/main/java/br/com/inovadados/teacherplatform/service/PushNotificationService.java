package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.PushSubscription;
import br.com.inovadados.teacherplatform.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.push.vapid-public-key}")
    private String vapidPublicKey;

    @Value("${app.push.vapid-private-key}")
    private String vapidPrivateKey;

    @Value("${app.push.vapid-subject}")
    private String vapidSubject;

    @Async
    @Transactional
    public void enviar(Long subscriptionId, String titulo, String corpo, String url) {
        PushSubscription sub = pushSubscriptionRepository.findById(subscriptionId).orElse(null);
        if (sub == null) return;

        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "titulo", titulo,
                    "corpo", corpo,
                    "url", url
            ));
        } catch (Exception e) {
            log.error("[Push] Erro ao serializar payload: {}", e.getMessage());
            return;
        }

        enviarComRetry(sub, payload, 3);
    }

    private void enviarComRetry(PushSubscription sub, String payload, int tentativasRestantes) {
        try {
            PushService pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            Notification notification = new Notification(sub.getEndpoint(), sub.getP256dh(), sub.getAuth(), payload);
            var response = pushService.send(notification);

            if (response.getStatusLine().getStatusCode() == 410) {
                // Subscription expirada — remover
                log.info("[Push] Subscription expirada (410), removendo endpoint: {}", sub.getEndpoint());
                pushSubscriptionRepository.delete(sub);
                return;
            }

            sub.setUltimoUso(java.time.OffsetDateTime.now());
            pushSubscriptionRepository.save(sub);

        } catch (Exception e) {
            if (tentativasRestantes > 1) {
                long delay = (long) Math.pow(2, 3 - tentativasRestantes) * 1000L;
                try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                enviarComRetry(sub, payload, tentativasRestantes - 1);
            } else {
                log.error("[Push] Falha após retries para subscription {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    @Async
    public void enviarParaUsuario(java.util.UUID usuarioId, String titulo, String corpo, String url) {
        pushSubscriptionRepository.findAllByUsuarioId(usuarioId)
                .forEach(sub -> enviar(sub.getId(), titulo, corpo, url));
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
}
