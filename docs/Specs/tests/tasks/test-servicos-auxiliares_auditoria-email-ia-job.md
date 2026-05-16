# Testes Unitários — AuditoriaService, EmailService, IaRateLimitService, ExpiracaoSessaoJob e ResponsavelService

> **Escopo:** Backend — serviços auxiliares de infraestrutura  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

Cinco serviços de suporte à infraestrutura: `AuditoriaService` (log de ações administrativas), `EmailService` (envio de e-mails de convite), `IaRateLimitService` (controle de uso de IA), `ExpiracaoSessaoJob` (job agendado para encerrar sessões expiradas) e `ResponsavelService` (painel de acompanhamento para pais).

---

## O que deve ser implementado

Criar cinco classes de teste cobrindo os comportamentos documentados na spec.

---

## Critérios de Aceite

**`AuditoriaServiceTest`:**
- [ ] `registrar`: `logAuditoriaRepository.save` chamado
- [ ] IP extraído de `X-Forwarded-For` quando presente
- [ ] IP extraído de `remoteAddr` quando sem header
- [ ] `realizadoEm != null`

**`EmailServiceTest`:**
- [ ] `enviarConvite`: `mailSender.send` chamado com `to = destinatário`
- [ ] Corpo do email contém o `linkAtivacao`
- [ ] Falha no envio → erro logado, sem propagação de exceção

**`IaRateLimitServiceTest`:**
- [ ] Uso dentro do limite → sem exceção
- [ ] Uso >= limite → lança `TooManyRequestsException`
- [ ] Janela de tempo: contagem reinicia após o período configurado

**`ExpiracaoSessaoJobTest`:**
- [ ] Sessão com `iniciadaEm + duracao < now` → `sessaoProvaService.encerrarPorExpiracao` chamado
- [ ] Sessão dentro do prazo → não encerrada
- [ ] Sessão já encerrada (`encerradaEm != null`) → ignorada
- [ ] 3 sessões expiradas, 2 ativas → apenas as 3 expiradas encerradas

**`ResponsavelServiceTest`:**
- [ ] Sem alunos vinculados → painel vazio sem exceção
- [ ] Com 1 aluno vinculado → painel contém dados do aluno (notas, frequência)

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/ServicosAuxiliares.md`
- **Seções:** `AuditoriaService`, `EmailService`, `IaRateLimitService`, `ExpiracaoSessaoJob`, `ResponsavelService`

---

## Detalhes Técnicos

**Localizações:**
- `src/test/java/.../service/AuditoriaServiceTest.java`
- `src/test/java/.../service/EmailServiceTest.java`
- `src/test/java/.../service/IaRateLimitServiceTest.java`
- `src/test/java/.../service/ExpiracaoSessaoJobTest.java`
- `src/test/java/.../service/ResponsavelServiceTest.java`

**Jobs agendados:** Chamar o método diretamente — não depender do `@Scheduled`.  
**EmailService:** Usar `@Mock JavaMailSender` — não conectar a servidor SMTP real.

---

## Definition of Done

- [ ] Cinco classes de teste criadas
- [ ] Todos os cenários cobertos (mínimo 3 testes por classe)
- [ ] Testes passam com `./mvnw test`
