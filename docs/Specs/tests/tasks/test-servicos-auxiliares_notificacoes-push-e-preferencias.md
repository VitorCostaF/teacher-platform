# Testes Unitários — PushNotificationService, PreferenciasNotificacaoService e NotificacaoAvaliacaoService

> **Escopo:** Backend — serviços de notificação push  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

Três serviços que compõem o sistema de notificações push: `PushNotificationService` (envio com retry), `PreferenciasNotificacaoService` (configurações por usuário) e `NotificacaoAvaliacaoService` (listener de evento que notifica alunos quando uma avaliação é publicada).

---

## O que deve ser implementado

Criar três classes de teste cobrindo os comportamentos da spec.

---

## Critérios de Aceite

**`PushNotificationServiceTest`:**
- [ ] Subscription não encontrada → sem exceção (falha silenciosa)
- [ ] Envio falha na 1ª tentativa → retry executado
- [ ] Todas as tentativas falham → erro logado, sem exceção propagada

**`PreferenciasNotificacaoServiceTest`:**
- [ ] `getPreferencias` sem cadastro → retorna objeto com todos campos `true` (padrão habilitado)
- [ ] `getPreferencias` com registro → retorna preferências salvas
- [ ] `salvar` sem preferências anteriores → `save` chamado com `usuarioId`
- [ ] `salvar` com preferências existentes → atualiza o mesmo registro (não cria duplicata)

**`NotificacaoAvaliacaoServiceTest`:**
- [ ] Evento com 1 turma → `pushNotificationService.enviar` chamado para cada aluno matriculado com subscription
- [ ] Aluno sem subscription → sem exceção, sem envio
- [ ] Evento com 2 turmas → alunos de ambas notificados

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/ServicosAuxiliares.md`
- **Seções:** `PushNotificationService`, `PreferenciasNotificacaoService`, `NotificacaoAvaliacaoService`

---

## Detalhes Técnicos

**Localizações:**
- `src/test/java/.../service/PushNotificationServiceTest.java`
- `src/test/java/.../service/PreferenciasNotificacaoServiceTest.java`
- `src/test/java/.../service/NotificacaoAvaliacaoServiceTest.java`

**Listener de evento:** Chamar o método listener diretamente passando um `AvaliacaoPublicadaEvent` mockado — não depender do publisher.

---

## Definition of Done

- [ ] Três classes de teste criadas
- [ ] Todos os cenários cobertos (mínimo 4 testes por classe)
- [ ] Testes passam com `./mvnw test`
