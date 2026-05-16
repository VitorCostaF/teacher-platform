package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.PreferenciasNotificacao;
import br.com.inovadados.teacherplatform.domain.entity.PushSubscription;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.RegistrarDeviceRequest;
import br.com.inovadados.teacherplatform.repository.PushSubscriptionRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.PreferenciasNotificacaoService;
import br.com.inovadados.teacherplatform.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificacaoController {

    private final PushNotificationService pushNotificationService;
    private final PreferenciasNotificacaoService preferenciasService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/notificacoes/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", pushNotificationService.getVapidPublicKey()));
    }

    @PostMapping("/notificacoes/registrar-device")
    public ResponseEntity<Void> registrarDevice(
            @Valid @RequestBody RegistrarDeviceRequest request,
            Authentication auth) {

        UUID usuarioId = resolverUsuarioId(auth);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));

        // TODO extrair um builder para subscription
        pushSubscriptionRepository.findByEndpoint(request.endpoint()).ifPresentOrElse(
                existing -> {
                    existing.setP256dh(request.p256dh());
                    existing.setAuth(request.auth());
                    pushSubscriptionRepository.save(existing);
                },
                () -> {
                    PushSubscription sub = new PushSubscription();
                    sub.setUsuario(usuario);
                    sub.setEndpoint(request.endpoint());
                    sub.setP256dh(request.p256dh());
                    sub.setAuth(request.auth());
                    pushSubscriptionRepository.save(sub);
                }
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/usuario/preferencias-notificacao")
    public ResponseEntity<PreferenciasNotificacao> getPreferencias(Authentication auth) {
        UUID usuarioId = resolverUsuarioId(auth);
        return ResponseEntity.ok(preferenciasService.getPreferencias(usuarioId));
    }

    @PutMapping("/usuario/preferencias-notificacao")
    public ResponseEntity<PreferenciasNotificacao> atualizarPreferencias(
            @RequestBody Map<String, Boolean> prefs,
            Authentication auth) {

        UUID usuarioId = resolverUsuarioId(auth);
        return ResponseEntity.ok(preferenciasService.atualizarPreferencias(usuarioId, prefs));
    }

    private UUID resolverUsuarioId(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName())
                .map(Usuario::getId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }
}
