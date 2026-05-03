# Plano de Implementação — backend-avaliacoes_endpoint-publicar

> **Task origem:** `docs/Tasks/backend-avaliacoes_endpoint-publicar.md`
> **Escopo:** Backend — Avaliações
> **Complexidade:** G
> **Sprint:** 3 — Criação com IA
> **Depende de:** `backend-model_avaliacoes-plan.md` (entidades Avaliacao, Questao, Entrega existentes)

---

## Contexto do Codebase

Entidades `Avaliacao`, `Questao`, `Entrega`, `TurmaAvaliacao` já existem com campo `status` (RASCUNHO/AGENDADA/PUBLICADA/ENCERRADA). Spring Security, JWT e GlobalExceptionHandler já estão configurados. Este plano implementa o ciclo de vida completo de provas/atividades: rascunho → revisão → publicação.

---

## Dependências a Adicionar no pom.xml

Nenhuma nova dependência além do que já está planejado.

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/request/SalvarRascunhoRequest.java`
```java
public record SalvarRascunhoRequest(
  @NotBlank String titulo,
  @NotNull TipoAvaliacaoEnum tipo,
  Integer duracaoMinutos,
  List<QuestaoRascunhoDto> questoes
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/PublicarAvaliacaoRequest.java`
```java
public record PublicarAvaliacaoRequest(
  @NotNull LocalDateTime disponivelEm,
  LocalDateTime encerraEm,
  @NotEmpty List<Long> turmasIds,
  boolean embaralharQuestoes,
  boolean embaralharAlternativas,
  String liberarGabaritoApos, // "entrega" | "encerramento" | "manual"
  BigDecimal peso
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/AvaliacaoResponse.java`
- Campos completos incluindo `status`, `questoes` (sem gabarito se contexto aluno)

`src/main/java/br/com/inovadados/teacherplatform/dto/response/PreviewAvaliacaoResponse.java`
- Igual a `AvaliacaoResponse` mas questões embaralhadas e sem gabarito

### Serviço

`src/main/java/br/com/inovadados/teacherplatform/service/AvaliacaoService.java`
- `salvarRascunho(SalvarRascunhoRequest req, UUID professorId)` → cria ou atualiza avaliação em status RASCUNHO
- `atualizarRascunho(Long id, SalvarRascunhoRequest req, UUID professorId)` → verifica propriedade; proibido se publicada
- `buscarAvaliacao(Long id, UUID usuarioId, PerfilEnum perfil)` → se aluno: remove gabarito das questões
- `preview(Long id, UUID professorId)` → embaralha questões e alternativas na memória (não persiste)
- `publicar(Long id, PublicarAvaliacaoRequest req, UUID professorId)` →
  1. Verificar propriedade da avaliação
  2. Se `disponivelEm` > agora → status AGENDADA; senão → PUBLICADA
  3. Criar registros em `turmas_avaliacoes` para cada turma selecionada
  4. Publicar evento `AvaliacaoPublicadaEvent` para notificações (Spring `ApplicationEvent`)
- `embaralharQuestoes(List<Questao> questoes, UUID seed)` → ordem determinística por aluno (seed = alunoId) garantindo reprodutibilidade para correção
- Regra: questões não podem ser removidas se status != RASCUNHO (lançar `OperacaoNaoPermitidaException`)

`src/main/java/br/com/inovadados/teacherplatform/event/AvaliacaoPublicadaEvent.java`
```java
public record AvaliacaoPublicadaEvent(Long avaliacaoId, List<Long> turmasIds) {}
```

`src/main/java/br/com/inovadados/teacherplatform/service/NotificacaoAvaliacaoService.java`
- `@EventListener(AvaliacaoPublicadaEvent.class)`
- Busca alunos das turmas → cria registros na tabela `notificacoes` para cada aluno
- Stub por ora; substituído pelo push real na task `global_notificacoes-push`

### Exceptions

`src/main/java/br/com/inovadados/teacherplatform/exception/OperacaoNaoPermitidaException.java`
`src/main/java/br/com/inovadados/teacherplatform/exception/AvaliacaoNaoEncontradaException.java`

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/AvaliacaoController.java`
- `@RestController @RequestMapping`
- `POST /provas/rascunho` e `POST /atividades/rascunho`
- `PUT /provas/:id` e `PUT /atividades/:id`
- `GET /provas/:id/preview`
- `POST /provas/:id/publicar` e `POST /atividades/:id/publicar`
- `GET /provas/:id` e `GET /atividades/:id`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `GlobalExceptionHandler` | Handlers: OperacaoNaoPermitidaException → 422, AvaliacaoNaoEncontradaException → 404 |
| `SecurityConfig` | Rotas `/provas/**` e `/atividades/**` acessíveis por PROFESSOR, ADMIN; preview visível para ALUNO |

---

## Ordem de Implementação

```
1. DTOs de request e response
2. Exceptions customizadas
3. AvaliacaoPublicadaEvent
4. NotificacaoAvaliacaoService (stub com log)
5. AvaliacaoService — na ordem: salvar → atualizar → buscar → preview → publicar
6. GlobalExceptionHandler — novos handlers
7. AvaliacaoController
8. SecurityConfig — novas regras
9. Testes unitários: lógica de embaralhamento, validação de estado
10. Testes de integração: ciclo rascunho → publicar
```

---

## Resumo

- **10 arquivos** a criar (DTOs, service, event, exceptions, controller)
- **2 arquivos** a modificar (GlobalExceptionHandler, SecurityConfig)
- **Nenhuma dependência nova** no pom.xml
- **Complexidade mantida:** G
