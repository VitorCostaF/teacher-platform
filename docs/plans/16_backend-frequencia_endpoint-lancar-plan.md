# Plano de Implementação — backend-frequencia_endpoint-lancar

> **Task origem:** `docs/Tasks/backend-frequencia_endpoint-lancar.md`
> **Escopo:** Backend — Frequência
> **Complexidade:** M
> **Sprint:** 2 — Gestão de Turmas
> **Depende de:** `backend-model_turmas-plan.md` (entidade Frequencia existente)

---

## Contexto do Codebase

Entidade `Frequencia` com campos `alunoId`, `turmaId`, `data`, `status` (PRESENTE/FALTA/FALTA_JUSTIFICADA) e `observacao` já existe com constraint UNIQUE(turma_id, aluno_id, data). Spring Security com JWT já configurado. `TurmaService` já existe para verificar permissão de acesso à turma.

---

## Dependências a Adicionar no pom.xml

Nenhuma nova. Usar Spring Async já disponível no Spring Boot.

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/request/LancarFrequenciaRequest.java`
```java
public record LancarFrequenciaRequest(
  @NotNull LocalDate data,
  @NotEmpty List<FrequenciaAlunoDto> alunos
) {}

public record FrequenciaAlunoDto(
  @NotNull UUID alunoId,
  @NotNull StatusFrequenciaEnum status,
  String observacao
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/FrequenciaResponse.java`
```java
public record FrequenciaResponse(
  Long id, LocalDate data, List<FrequenciaAlunoDto> alunos
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/HistoricoFrequenciaResponse.java`
```java
public record HistoricoFrequenciaResponse(
  double percentualPresenca,
  int totalAulas,
  int totalPresencas,
  int totalFaltas,
  List<DiaFrequenciaDto> calendario
) {}
```

### Enum

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/StatusFrequenciaEnum.java`
```java
public enum StatusFrequenciaEnum { PRESENTE, FALTA, FALTA_JUSTIFICADA }
```

### Serviço

`src/main/java/br/com/inovadados/teacherplatform/service/FrequenciaService.java`
- `buscarPorData(Long turmaId, LocalDate data)` → `Optional<FrequenciaResponse>` (retorna null se não existe)
- `lancarFrequencia(Long turmaId, LancarFrequenciaRequest req)` → cria ou substitui registros do dia; persiste em batch; dispara verificação de alerta assíncrona
- `editarFrequencia(Long turmaId, Long frequenciaId, LancarFrequenciaRequest req)` → atualiza registros existentes
- `buscarHistorico(Long turmaId, UUID alunoId)` → calcula percentual `(presenças / total aulas até hoje) * 100`, monta calendário
- `verificarAlertas(Long turmaId, UUID alunoId)` → privado, chamado via `@Async`: se percentual < 75% ou 3 faltas consecutivas, enfileira notificação

`src/main/java/br/com/inovadados/teacherplatform/service/AlertaFrequenciaService.java`
- `@Async @EventListener` ou chamado diretamente por `FrequenciaService`
- `enfileirarAlertaFalta(Long turmaId, UUID alunoId, String motivo)` → salva em tabela `alertas` ou publica em fila (usar `ApplicationEvent` por ora; substituir por RabbitMQ/Kafka quando integrar notificações push)

### Repository adicional

`src/main/java/br/com/inovadados/teacherplatform/repository/FrequenciaRepository.java`
- Extends `JpaRepository<Frequencia, Long>`
- `Optional<Frequencia> findByTurmaIdAndData(Long turmaId, LocalDate data)`
- `List<Frequencia> findByTurmaIdAndAlunoIdOrderByDataAsc(Long turmaId, UUID alunoId)`
- `@Query` para contar presenças e total de aulas

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/FrequenciaController.java`
- `@RestController @RequestMapping("/turmas/{turmaId}/frequencia")`
- `GET ?data=YYYY-MM-DD` → retorna registro ou 204 se não existe
- `POST /` → criar novo registro
- `PUT /:frequenciaId` → editar
- `GET /alunos/:alunoId` → histórico

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `src/main/java/.../exception/GlobalExceptionHandler.java` | Handler para DataIntegrityViolationException → 409 quando tentar lançar frequência duplicada |
| `src/main/java/.../TeacherPlatformApplication.java` | Adicionar `@EnableAsync` para alertas assíncronos |

---

## Ordem de Implementação

```
1. Enum StatusFrequenciaEnum
2. DTOs de request e response
3. FrequenciaRepository — queries customizadas
4. AlertaFrequenciaService (stub que loga por enquanto)
5. FrequenciaService — métodos na ordem: buscar → lancar → editar → histórico → alertas
6. FrequenciaController
7. GlobalExceptionHandler — handler de conflito 409
8. Habilitar @EnableAsync na Application
9. Testes unitários: FrequenciaService com mocks
10. Testes de integração: POST e GET /frequencia com banco real
```

---

## Resumo

- **8 arquivos** a criar (3 DTOs, 1 enum, 2 services, 1 repository, 1 controller)
- **2 arquivos** a modificar (GlobalExceptionHandler, Application)
- **Nenhuma dependência nova** no pom.xml
- **Complexidade mantida:** M
