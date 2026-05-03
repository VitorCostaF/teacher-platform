# Plano de Implementação — backend-turmas_endpoint-listar

> **Task origem:** `docs/Tasks/backend-turmas_endpoint-listar.md`
> **Escopo:** Backend — Turmas e Alunos
> **Complexidade:** M
> **Sprint:** 2 — Gestão de Turmas
> **Depende de:** `backend-model_turmas-plan.md` (entidades Turma, Matricula, Frequencia existentes)

---

## Contexto do Codebase

Entidades `Turma`, `Matricula`, `PeriodoLetivo`, `Usuario` e repositories correspondentes já existem (definidos nos planos de model). Spring Security com `JwtAuthFilter` já está configurado (`backend-auth_endpoint-post-login-plan.md`). Este plano implementa os endpoints REST de gestão de turmas e alunos com controle de acesso por perfil.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- Parsing de CSV/XLSX para importação de alunos -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-csv</artifactId>
  <version>1.11.0</version>
</dependency>
<dependency>
  <groupId>org.apache.poi</groupId>
  <artifactId>poi-ooxml</artifactId>
  <version>5.3.0</version>
</dependency>
```

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/response/TurmaResumoResponse.java`
```java
public record TurmaResumoResponse(
  Long id, String nome, String disciplina, int totalAlunos,
  String proximaAula, PendenciasDto pendencias
) {}

public record PendenciasDto(int frequenciasNaoLancadas, int atividadesNaoCorrigidas) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/TurmaDetalheResponse.java`
- Todos os campos de `TurmaResumoResponse` + grade horária, período letivo, professor

`src/main/java/br/com/inovadados/teacherplatform/dto/response/AlunoTurmaResponse.java`
```java
public record AlunoTurmaResponse(UUID id, String nome, String email, String avatarUrl, LocalDate matriculadoEm) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/AdicionarAlunoRequest.java`
```java
public record AdicionarAlunoRequest(UUID alunoId, String email) {} // um ou outro obrigatório
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/CriarTurmaRequest.java`
```java
public record CriarTurmaRequest(
  @NotBlank String nome,
  @NotBlank String disciplina,
  @NotNull Long periodoLetivoId,
  @NotNull UUID professorId,
  String gradeHoraria
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/ImportacaoResponse.java`
```java
public record ImportacaoResponse(int importados, List<ErroImportacao> erros) {}
public record ErroImportacao(int linha, String motivo) {}
```

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/TurmaService.java`
- `listarTurmasProfessor(UUID professorId, Long periodoId)` → calcula pendências (subquery de frequências não lançadas e entregas não corrigidas)
- `listarTurmasAdmin(Long escolaId, Long periodoId)` → todas as turmas da escola
- `buscarTurma(Long turmaId, UUID usuarioAutenticado)` → verifica permissão (professor só vê sua turma)
- `criarTurma(CriarTurmaRequest req)` → somente admin
- `editarTurma(Long id, CriarTurmaRequest req)` → somente admin
- `encerrarTurma(Long id)` → soft delete: seta `encerrada_em = NOW()`
- `listarAlunos(Long turmaId)` → alunos com `matricula.ativo = true`
- `adicionarAluno(Long turmaId, AdicionarAlunoRequest req)` → por UUID ou dispara convite por e-mail
- `removerAluno(Long turmaId, UUID alunoId)` → soft delete na matrícula
- `importarAlunos(Long turmaId, MultipartFile file)` → processa CSV/XLSX linha a linha; acumula erros sem interromper linhas válidas

`src/main/java/br/com/inovadados/teacherplatform/service/PlanilhaParserService.java`
- `parsearCSV(InputStream)` → `List<Map<String, String>>`
- `parsearXLSX(InputStream)` → `List<Map<String, String>>`
- Detectar formato pelo content-type / extensão do arquivo

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/TurmaController.java`
- `@RestController @RequestMapping("/turmas")`
- `GET /professor/turmas?periodo=:id` → `@PreAuthorize("hasAnyRole('PROFESSOR','ADMIN')")`
- `GET /turmas/:id` → verifica permissão no service
- `POST /admin/turmas` → `@PreAuthorize("hasRole('ADMIN')")`
- `PUT /admin/turmas/:id` → `@PreAuthorize("hasRole('ADMIN')")`
- `PATCH /admin/turmas/:id/encerrar` → `@PreAuthorize("hasRole('ADMIN')")`
- `GET /turmas/:id/alunos`
- `POST /turmas/:id/alunos`
- `DELETE /turmas/:id/alunos/:alunoId`
- `POST /turmas/:id/alunos/importar` → recebe `MultipartFile`

### Exception

`src/main/java/br/com/inovadados/teacherplatform/exception/TurmaNaoEncontradaException.java`
`src/main/java/br/com/inovadados/teacherplatform/exception/AcessoNegadoException.java`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `pom.xml` | Adicionar commons-csv, poi-ooxml |
| `src/main/java/.../exception/GlobalExceptionHandler.java` | Handlers para TurmaNaoEncontradaException (404) e AcessoNegadoException (403) |
| `src/main/java/.../security/SecurityConfig.java` | Liberar rotas `/professor/**` e `/admin/**` com `hasRole` correto |

---

## Arquivos de Referência (apenas leitura)

| Arquivo | Por que consultar |
|---------|------------------|
| `docs/plans/backend-model_turmas-plan.md` | Nomes exatos das entidades Turma, Matricula, PeriodoLetivo |
| `docs/plans/backend-auth_endpoint-post-login-plan.md` | Como o SecurityConfig está estruturado |

---

## Ordem de Implementação

```
1. pom.xml — adicionar dependências CSV/XLSX
2. DTOs de request e response (TurmaResumoResponse, AlunoTurmaResponse, etc.)
3. PlanilhaParserService (independente, testável isoladamente)
4. TurmaService (métodos na ordem: listar → buscar → criar → editar → encerrar → alunos)
5. Exceptions customizadas
6. GlobalExceptionHandler — adicionar handlers novos
7. TurmaController
8. SecurityConfig — adicionar regras de acesso
9. Testes unitários: TurmaService (mock repository) e PlanilhaParserService
10. Testes de integração: endpoints principais com banco real
```

---

## Resumo

- **9 arquivos** a criar (2 DTOs de grupo, 2 serviços, 1 controller, 2 exceptions, 2 extras)
- **3 arquivos** a modificar (pom.xml, GlobalExceptionHandler, SecurityConfig)
- **Bibliotecas a adicionar:** commons-csv, poi-ooxml
- **Complexidade mantida:** M
