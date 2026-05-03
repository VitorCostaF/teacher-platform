# Plano de Implementação — global_notificacoes-push

> **Task origem:** `docs/Tasks/global_notificacoes-push.md`
> **Escopo:** Fullstack — Notificações Push
> **Complexidade:** G
> **Sprint:** 7 — Polimento e Infraestrutura
> **Depende de:** `global_offline-e-pwa-plan.md`, `backend-frequencia_endpoint-lancar-plan.md`

---

## Contexto do Codebase

**Frontend:** Service Worker registrado via vite-plugin-pwa. `apiClient`, `toastEmitter`, `useOfflineSync` disponíveis.
**Backend:** `AlertaFrequenciaService` (stub), `NotificacaoAvaliacaoService` (stub), entidades `Usuario` já existem.

---

## Parte 1 — Backend

### Dependências (pom.xml)

```xml
<!-- Web Push (VAPID) -->
<dependency>
  <groupId>nl.martijndwars</groupId>
  <artifactId>web-push</artifactId>
  <version>5.1.1</version>
</dependency>
<dependency>
  <groupId>org.bouncycastle</groupId>
  <artifactId>bcprov-jdk18on</artifactId>
  <version>1.78.1</version>
</dependency>
```

### Configurações (application.properties)

```properties
app.push.vapid-public-key=<gerado via web-push key generation>
app.push.vapid-private-key=<privado>
app.push.vapid-subject=mailto:admin@teacherplatform.com
```

### Migration

`src/main/resources/db/migration/V13__create_push_subscriptions.sql`
```sql
CREATE TABLE push_subscriptions (
  id BIGSERIAL PRIMARY KEY,
  usuario_id UUID NOT NULL REFERENCES usuarios(id),
  endpoint TEXT NOT NULL UNIQUE,
  p256dh VARCHAR(500) NOT NULL,
  auth VARCHAR(200) NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ultimo_uso TIMESTAMPTZ
);
CREATE INDEX idx_push_usuario ON push_subscriptions(usuario_id);

CREATE TABLE preferencias_notificacao (
  usuario_id UUID PRIMARY KEY REFERENCES usuarios(id),
  falta_aluno BOOLEAN NOT NULL DEFAULT TRUE,
  queda_frequencia BOOLEAN NOT NULL DEFAULT TRUE,
  prazo_prova BOOLEAN NOT NULL DEFAULT TRUE
);
```

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/request/RegistrarDeviceRequest.java`
```java
public record RegistrarDeviceRequest(String endpoint, String p256dh, String auth) {}
```

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/PushNotificationService.java`
- Usa `nl.martijndwars.webpush.PushService`
- `enviar(Long subscriptionId, String titulo, String corpo, String url)` → chama Web Push API
- Trata `410 Gone`: subscription expirada → soft delete do registro
- Retry: tentativas com exponential backoff para erros temporários
- Roda via `@Async` para não bloquear o fluxo principal

`src/main/java/br/com/inovadados/teacherplatform/service/PreferenciasNotificacaoService.java`
- `getPreferencias(UUID usuarioId)` → busca ou cria com defaults
- `atualizarPreferencias(UUID usuarioId, Map<String, Boolean> prefs)` → atualiza campos

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/NotificacaoController.java`
- `POST /notificacoes/registrar-device`
- `GET /usuario/preferencias-notificacao`
- `PUT /usuario/preferencias-notificacao`
- `GET /notificacoes/vapid-public-key` → retorna chave pública VAPID (endpoint público)

### Atualizar Stubs

Substituir stubs em `AlertaFrequenciaService` e `NotificacaoAvaliacaoService` pelas chamadas reais ao `PushNotificationService`.

---

## Parte 2 — Frontend

### Hook de Permissão e Registro

`frontend/src/hooks/usePushNotifications.ts`
- Verificar `'Notification' in window && 'serviceWorker' in navigator`
- NÃO solicitar permissão imediatamente — chamar apenas após 1ª interação significativa (ex: depois de completar uma entrega ou lançar frequência)
- `requestPermission()` → `Notification.requestPermission()`; se granted → chamar `subscribeToPush()`
- `subscribeToPush()` → `registration.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: vapidKey })` → chamar `apiClient.post('/notificacoes/registrar-device', subscription)`

### Service Worker Push Handler

Adicionar ao Service Worker (via injectManifest ou workbox custom handlers):
```javascript
self.addEventListener('push', (event) => {
  const data = event.data?.json()
  event.waitUntil(
    self.registration.showNotification(data.titulo, {
      body: data.corpo,
      data: { url: data.url },
    })
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  event.waitUntil(clients.openWindow(event.notification.data.url))
})
```

### Componente de Preferências

`frontend/src/features/profile/components/PreferenciasNotificacaoForm.tsx`
- 3 toggles: "Faltas do meu filho", "Queda de frequência", "Lembretes de prazo"
- Salva ao trocar cada toggle (`onChange`)

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `pom.xml` | Adicionar web-push, bcprov |
| `application.properties` | VAPID keys e subject |
| `AlertaFrequenciaService.java` | Substituir stub por chamada real ao PushNotificationService |
| `NotificacaoAvaliacaoService.java` | Substituir stub por chamada real |
| `SecurityConfig.java` | `/notificacoes/vapid-public-key` como rota pública |
| `frontend/vite.config.ts` | Adicionar push event handler no Service Worker via injectManifest |

---

## Ordem de Implementação

```
Backend:
1. pom.xml — web-push, bcprov
2. application.properties — VAPID keys
3. Migration V13 — push_subscriptions, preferencias_notificacao
4. PushNotificationService
5. PreferenciasNotificacaoService
6. NotificacaoController
7. Atualizar AlertaFrequenciaService e NotificacaoAvaliacaoService

Frontend:
8. usePushNotifications hook (pedir permissão após interação)
9. Service Worker push event handler
10. PreferenciasNotificacaoForm
11. Integrar hook em pontos de 1ª interação significativa

Testes:
12. Notificação de falta chega em < 5min
13. Subscription expirada (410) removida automaticamente
14. Preferências respeitadas (toggle desativado não recebe)
```

---

## Checklist de Validação

- [ ] Permissão não solicitada imediatamente no login
- [ ] Notificação clicada abre rota correta
- [ ] Falta → notificação ao responsável em < 5min
- [ ] Frequência < 75% dispara alerta
- [ ] Usuário pode desativar tipos específicos
- [ ] 410 Gone remove subscription expirada
- [ ] Funciona em Chrome e Safari (iOS 16.4+)

---

## Resumo

**Backend:**
- **6 arquivos** a criar (DTO, 2 services, controller, migration V13)
- **4 arquivos** a modificar (pom.xml, properties, 2 stubs, SecurityConfig)

**Frontend:**
- **2 arquivos** a criar (hook, PreferenciasNotificacaoForm)
- **1 arquivo** a modificar (vite.config.ts)

- **Complexidade mantida:** G
