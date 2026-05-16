# Spec de Testes Unitários — Serviços Auxiliares

Este documento agrupa os serviços com lógica mais simples ou fortemente dependentes de integrações externas. Para cada um, são especificados os comportamentos testáveis de forma unitária.

---

## 1. ObservacaoService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/ObservacaoService.java`  
**Dependências:** `ObservacaoRepository`, `UsuarioRepository`, `MatriculaRepository`

### Método: `criar(Long turmaId, UUID alunoId, UUID professorId, ObservacaoRequest)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Cria observação vinculada | dados válidos | `observacaoRepository.save` chamado com turmaId, alunoId e professorId corretos |
| 2 | `criadoEm` preenchido | qualquer | campo `criadoEm != null` |

### Método: `listar(Long turmaId, UUID alunoId, PerfilEnum perfil)`

| # | Cenário | Perfil | Comportamento |
|---|---------|--------|---------------|
| 3 | PROFESSOR vê suas observações | `PROFESSOR` | filtro aplicado por autor |
| 4 | ADMIN vê todas | `ADMIN` | sem filtro por autor |
| 5 | RESPONSAVEL vê todas | `RESPONSAVEL` | sem filtro por autor |

---

## 2. FeedAlunoService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/FeedAlunoService.java`  
**Dependências:** `AvaliacaoRepository`, `EntregaRepository`, `MatriculaRepository`

### Método: `getFeed(UUID alunoId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Retorna apenas avaliações PUBLICADAS das turmas do aluno | avaliação em RASCUNHO | não aparece no feed |
| 2 | Inclui status da entrega do aluno | aluno entregou | `statusEntrega` preenchido |
| 3 | Status `NAO_INICIADO` quando sem entrega | sem entrega | `statusEntrega = "NAO_INICIADO"` |

---

## 3. RelatorioService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/RelatorioService.java`  
**Dependências:** `RelatorioJobRepository` (ou similar), serviço de PDF

### Método: `solicitarRelatorioPDF(String tipo, Long id, UUID usuarioId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Job criado com status PENDENTE | qualquer | retorna `RelatorioStatusResponse` com `status = PENDENTE` |
| 2 | `jobId` único gerado | duas chamadas | jobIds diferentes |

### Método: `getStatus(String jobId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 3 | Job não encontrado | id inexistente | lança exceção ou retorna `NOT_FOUND` |
| 4 | Retorna status atual | job em PROCESSANDO | `status = PROCESSANDO` |

---

## 4. PushNotificationService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/PushNotificationService.java`  
**Dependências:** `PushSubscriptionRepository`, cliente de Web Push

### Método: `enviar(Long subscriptionId, String titulo, String corpo, String url)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Subscription não encontrada | `findById` retorna empty | não lança exceção (falha silenciosa) |
| 2 | Erro no envio com retry | envio falha na 1ª tentativa | retry executado conforme configuração |
| 3 | Falha definitiva logada | todas as tentativas falham | erro logado, sem propagação |

---

## 5. PreferenciasNotificacaoService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/PreferenciasNotificacaoService.java`  
**Dependências:** `PreferenciasNotificacaoRepository`

### Método: `getPreferencias(UUID usuarioId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Sem preferências cadastradas | `findByUsuarioId` retorna empty | retorna objeto com todos os campos como `true` (padrão habilitado) |
| 2 | Com preferências | registro existente | retorna preferências salvas |

### Método: `salvar(UUID usuarioId, PreferenciasRequest)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 3 | Cria novo registro | sem preferências anteriores | `save` chamado com `usuarioId` correto |
| 4 | Atualiza existente | preferências já existem | mesmo registro atualizado, não cria duplicata |

---

## 6. AuditoriaService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/AuditoriaService.java`  
**Dependências:** `LogAuditoriaRepository`

### Método: `registrar(UUID adminId, String acao, String detalhes, HttpServletRequest)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Log salvo | qualquer | `logAuditoriaRepository.save` chamado |
| 2 | IP capturado | `X-Forwarded-For` presente | IP correto no log |
| 3 | IP do remoteAddr | sem `X-Forwarded-For` | usa `request.getRemoteAddr()` |
| 4 | `realizadoEm` preenchido | qualquer | `realizadoEm != null` |

---

## 7. EmailService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/EmailService.java`  
**Dependências:** `JavaMailSender` ou `MailSender`

### Método: `enviarConvite(String destinatario, String linkAtivacao)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Email enviado para destinatário correto | `destinatario = "x@y.com"` | `mailSender.send` chamado com `to = "x@y.com"` |
| 2 | Link no corpo do email | `link = "http://..."` | corpo contém o link |
| 3 | Falha de envio não propaga | `mailSender` lança exceção | erro logado, sem propagação (ou exceção controlada) |

---

## 8. NotificacaoAvaliacaoService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/NotificacaoAvaliacaoService.java`  
**Dependências:** `PushNotificationService`, `MatriculaRepository`, `PushSubscriptionRepository`

### Método: listener de `AvaliacaoPublicadaEvent`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Notifica alunos da turma | evento com turmaId | `pushNotificationService.enviar` chamado para cada aluno matriculado |
| 2 | Alunos sem subscription ignorados | aluno sem sub cadastrada | nenhuma exceção; sem envio |
| 3 | Múltiplas turmas | evento com 2 turmas | alunos de ambas as turmas notificados |

---

## 9. IaRateLimitService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/IaRateLimitService.java`  
**Dependências:** `IaUsageLogRepository` ou Redis

### Comportamentos a testar

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Dentro do limite | tokens consumidos < limite | não lança exceção |
| 2 | Limite de tokens atingido | tokens >= limite configurado | lança `TooManyRequestsException` |
| 3 | Janela de tempo respeitada | limite por hora/dia | contagem reinicia após a janela |

---

## 10. ExpiracaoSessaoJob

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/ExpiracaoSessaoJob.java`  
**Dependências:** `SessaoProvaRepository`, `SessaoProvaService`

### Método: `expirarSessoes()` (agendado via `@Scheduled`)

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Sessão expirada encontrada | `iniciadaEm + duracao < now` | `sessaoProvaService.encerrarPorExpiracao` chamado |
| 2 | Sessão ainda dentro do prazo | não expirou | não encerrada |
| 3 | Sessão já encerrada | `encerradaEm != null` | ignorada |
| 4 | Múltiplas sessões | 3 expiradas, 2 ativas | apenas as 3 expiradas encerradas |

---

## 11. ResponsavelService

**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/ResponsavelService.java`  
**Dependências:** `VinculoResponsavelRepository`, `MatriculaRepository`, `EntregaRepository`, `RegistroFrequenciaRepository`

### Método: `getPainel(UUID responsavelId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Responsável sem alunos vinculados | `findByResponsavelId` retorna `[]` | retorna painel vazio sem exceção |
| 2 | Dados de aluno incluídos | 1 aluno vinculado | painel contém notas e frequência do aluno |

---

## Observações Gerais

- Serviços com `@Async` devem ser testados de forma síncrona — usar `@SpringBootTest` com `AsyncConfigurer` mockado, ou extrair a lógica para método síncrono auxiliar.
- Serviços que integram JavaMail ou Web Push devem usar mocks — nunca conectar a serviços reais nos testes unitários.
- Jobs agendados (`@Scheduled`) devem ter o método principal testado diretamente sem depender do agendador do Spring.
