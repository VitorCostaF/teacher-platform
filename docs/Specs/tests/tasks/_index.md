# Índice de Tasks — Testes Unitários Backend

Todas as tasks abaixo derivam das specs em `docs/Specs/tests/unit/`.  
Cada task descreve a implementação dos testes unitários de uma classe ou grupo de classes.

---

## Tabela de Tasks

| # | Arquivo | Classe(s) | Complexidade | Depende de |
|---|---------|-----------|-------------|------------|
| 1 | `test-jwt-service_geracao-e-validacao-tokens.md` | `JwtService` | P | — |
| 2 | `test-rate-limit-service_controle-ip-e-bloqueio-conta.md` | `RateLimitService` | M | — |
| 3 | `test-auth-service_fluxo-login.md` | `AuthService.login` | M | — |
| 4 | `test-auth-service_refresh-e-logout.md` | `AuthService.refresh` + `.logout` | M | #3 (mesma classe) |
| 5 | `test-flashcard-service_algoritmo-sm2.md` | `FlashcardService` (SM-2) | M | — |
| 6 | `test-flashcard-service_priorizacao-e-historico.md` | `FlashcardService.getFlashcardsPriorizados` | P | #5 (mesma classe) |
| 7 | `test-gamificacao-service_calculo-pontos-e-conquistas.md` | `GamificacaoService` | M | — |
| 8 | `test-frequencia-service_logica-consecutivas-e-percentual.md` | `FrequenciaService` | M | — |
| 9 | `test-atividade-service_calculo-nota-automatica.md` | `AtividadeService` (nota + status) | M | — |
| 10 | `test-atividade-service_entrega-e-rascunho.md` | `AtividadeService.entregar` + `salvarRascunho` | M | #9 (mesma classe) |
| 11 | `test-avaliacao-service_ciclo-de-vida-rascunho-publicacao.md` | `AvaliacaoService` | M | — |
| 12 | `test-sessao-prova-service_tempo-restante-e-ciclo-sessao.md` | `SessaoProvaService` | M | — |
| 13 | `test-dashboard-service_tendencia-posicao-situacao.md` | `DashboardService` | M | — |
| 14 | `test-alerta-frequencia-service_disparo-e-preferencias.md` | `AlertaFrequenciaService` | P | — |
| 15 | `test-turma-service_acesso-e-matricula.md` | `TurmaService` | P | — |
| 16 | `test-admin-service_dashboard-e-gestao-usuarios.md` | `AdminService` | G | — |
| 17 | `test-servicos-auxiliares_observacao-feed-relatorio.md` | `ObservacaoService`, `FeedAlunoService`, `RelatorioService` | P | — |
| 18 | `test-servicos-auxiliares_notificacoes-push-e-preferencias.md` | `PushNotificationService`, `PreferenciasNotificacaoService`, `NotificacaoAvaliacaoService` | P | — |
| 19 | `test-servicos-auxiliares_auditoria-email-ia-job.md` | `AuditoriaService`, `EmailService`, `IaRateLimitService`, `ExpiracaoSessaoJob`, `ResponsavelService` | P | — |

---

## Ordem Sugerida de Implementação

As tasks sem dependências podem ser feitas em paralelo. A ordem abaixo minimiza bloqueios:

### Fase 1 — Serviços de infraestrutura (sem dependências entre si)
1. `JwtService` — base do sistema de auth
2. `RateLimitService` — depende apenas de Redis mockado
3. `AuthService.login` — usa JwtService e RateLimitService mockados

### Fase 2 — Lógica de negócio pura (sem dependências entre si)
4. `FlashcardService` (SM-2 primeiro, depois priorização)
5. `GamificacaoService`
6. `FrequenciaService`
7. `AtividadeService` (nota primeiro, depois entrega)
8. `AvaliacaoService`

### Fase 3 — Serviços compostos
9. `AuthService.refresh` e `logout` (adiciona à classe já criada)
10. `SessaoProvaService` (depende de AtividadeService mockado)
11. `DashboardService`
12. `AlertaFrequenciaService`
13. `TurmaService`
14. `AdminService`

### Fase 4 — Serviços auxiliares
15. `ObservacaoService`, `FeedAlunoService`, `RelatorioService`
16. Serviços de notificação push
17. `AuditoriaService`, `EmailService`, `IaRateLimitService`, `ExpiracaoSessaoJob`, `ResponsavelService`

---

## Convenções para Todos os Testes

| Item | Padrão |
|------|--------|
| Framework | JUnit 5 (Jupiter) + Mockito |
| Localização | `src/test/java/br/com/inovadados/teacherplatform/service/` |
| Nomenclatura de métodos | `deveFazer_quandoCondicao()` ou `nomeDoMetodo_cenario_resultadoEsperado()` |
| Injeção de `@Value` | `ReflectionTestUtils.setField(...)` |
| Spring Context | **Não usar** `@SpringBootTest` nos testes unitários — Mockito puro |
| Execução | `./mvnw test -Dtest=NomeDaClasseTest` |
| BigDecimal | Sempre usar `new BigDecimal("valor")` — nunca `new BigDecimal(double)` |

---

## Spec de Referência

Todos os cenários, entradas e saídas esperadas estão documentados em:  
`docs/Specs/tests/unit/[NomeDoServico].md`
