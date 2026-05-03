# Plano de Implementação — backend-aluno_endpoint-entregas

> **Task origem:** `docs/Tasks/backend-aluno_endpoint-entregas.md`
> **Escopo:** Backend — Entregas de Atividades e Provas
> **Complexidade:** G
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `backend-model_avaliacoes-plan.md` (entidades Entrega, SessaoProva, Questao)

---

## Contexto do Codebase

Entidade `Entrega` com UNIQUE(avaliacao_id, aluno_id) já existe. Entidade `SessaoProva` com campos `iniciadaEm`, `entregueEm`, `respostas` (JSONB) já existe. Spring Security com JWT configurado. `@EnableAsync` já ativado. Este plano implementa o ciclo completo de entrega com sessão de prova e job de expiração automática.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- Scheduler para job de expiração -->
<!-- Já disponível via @EnableScheduling no Spring Boot -->
```

Nenhuma dependência nova — usar `@EnableScheduling` e `@Scheduled`.

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/response/AtividadeDetalheResponse.java`
```java
public record AtividadeDetalheResponse(
  Long id, String titulo, String disciplina, String tipo,
  LocalDateTime prazo, boolean permiteAtraso, boolean gabarito_disponivel,
  String statusAluno,  // "NAO_INICIADO" | "RASCUNHO" | "ENTREGUE"
  List<QuestaoAtividadeDto> questoes,
  Map<Long, Object> respostasRascunho  // pré-preenchido se rascunho existe
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/SalvarRascunhoAtividadeRequest.java`
```java
public record SalvarRascunhoAtividadeRequest(Map<Long, Object> respostas) {} // questaoId -> resposta
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/EntregarResponse.java`
```java
public record EntregarResponse(
  Long entregaId, BigDecimal nota,
  boolean gabaritoDisponivel, List<GabaritoQuestaoDto> gabarito
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/SessaoProvaResponse.java`
```java
public record SessaoProvaResponse(
  Long sessaoId, LocalDateTime iniciadaEm, int duracaoMinutos,
  Map<Long, Object> respostasParciaiis
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/AutosaveProvaRequest.java`
```java
public record AutosaveProvaRequest(
  Map<Long, Object> respostas,
  String eventoVisibilidade  // "visible" | "hidden" | null
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/ResultadoEntregaResponse.java`
```java
public record ResultadoEntregaResponse(
  BigDecimal nota, BigDecimal mediaTurma,
  boolean gabaritoDisponivel,
  List<GabaritoQuestaoDto> gabarito,
  List<AnaliseTopicoDto> analiseTopicos  // IA
) {}
```

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/AtividadeService.java`
- `getAtividade(Long id, UUID alunoId)` → retorna avaliação + status do aluno (rascunho, entregue, não iniciado)
- `salvarRascunho(Long id, SalvarRascunhoAtividadeRequest req, UUID alunoId)` → atualiza campo `respostas` em `Entrega` com status RASCUNHO
- `entregar(Long id, SalvarRascunhoAtividadeRequest req, UUID alunoId)` →
  1. Verificar prazo; se vencido e `permiteAtraso=false` → 422
  2. Salvar respostas
  3. Calcular nota automática para questões objetivas
  4. Marcar entrega como ENTREGUE
  5. Se gabarito disponível: retornar gabarito na response

`src/main/java/br/com/inovadados/teacherplatform/service/SessaoProvaService.java`
- `iniciar(Long provaId, UUID alunoId)` → verifica se já existe sessão ativa (retorna existente); cria com `iniciadaEm = NOW()` do servidor
- `autosave(Long sessaoId, AutosaveProvaRequest req, UUID alunoId)` → atualiza respostas parciais; registra evento de visibilidade se enviado
- `entregar(Long provaId, Long sessaoId, UUID alunoId)` → valida sessão; calcula nota; marca `entregueEm = NOW()`
- `calcularNotaAutomatica(List<Questao> questoes, Map<Long, Object> respostas)` → só questões objetivas; soma pontos proporcionais
- `calcularTempoRestante(SessaoProva sessao)` → `duracao - (NOW() - iniciadaEm)`

`src/main/java/br/com/inovadados/teacherplatform/service/ExpiracaoSessaoJob.java`
- `@Scheduled(fixedRate = 60000)` — roda a cada minuto
- Busca sessões onde `iniciadaEm + duracaoMinutos < NOW()` e `entregueEm IS NULL`
- Para cada uma: chama `SessaoProvaService.entregar()` com respostas parciais existentes
- Log de cada sessão expirada; em caso de erro, marcar sessão como EXPIRADA_COM_ERRO para investigação

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/EntregaController.java`
- `GET /atividades/:id`
- `PUT /atividades/:id/rascunho`
- `POST /atividades/:id/entregar`
- `POST /provas/:id/iniciar`
- `PUT /provas/:id/sessoes/:sessaoId/autosave`
- `POST /provas/:id/sessoes/:sessaoId/entregar`
- `GET /aluno/avaliacoes/:entregaId/resultado`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `TeacherPlatformApplication.java` | Adicionar `@EnableScheduling` junto com `@EnableAsync` já existente |
| `SecurityConfig` | Rotas `/atividades/**` e `/provas/:id/iniciar`, `/provas/:id/sessoes/**` somente ALUNO |

---

## Ordem de Implementação

```
1. DTOs de request e response
2. AtividadeService — getAtividade, salvarRascunho, entregar
3. SessaoProvaService — iniciar, autosave, entregar, calcularNotaAutomatica
4. ExpiracaoSessaoJob — @Scheduled
5. EntregaController
6. TeacherPlatformApplication — @EnableScheduling
7. SecurityConfig
8. Testes unitários: calcularNotaAutomatica, calcularTempoRestante
9. Testes de integração: ciclo iniciar → autosave → entregar com sessão real
10. Teste do job de expiração com tempo acelerado (mock do relógio)
```

---

## Resumo

- **11 arquivos** a criar (DTOs, 3 serviços, 1 controller)
- **2 arquivos** a modificar (Application, SecurityConfig)
- **Nenhuma dependência nova** (usa @EnableScheduling já disponível)
- **Complexidade mantida:** G
