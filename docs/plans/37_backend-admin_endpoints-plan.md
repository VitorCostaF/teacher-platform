# Plano de Implementação — backend-admin_endpoints

> **Task origem:** `docs/Tasks/backend-admin_endpoints.md`
> **Escopo:** Backend — Área Administrativa
> **Complexidade:** G
> **Sprint:** 6 — Responsável e Administração
> **Depende de:** `backend-model_usuario-plan.md`, `backend-dashboard_endpoint-professor-plan.md`

---

## Contexto do Codebase

Entidades `Usuario`, `Escola`, `Turma`, `Matricula` já existem. `DashboardService` disponível. `PlanilhaParserService` criado no plano de turmas. `TokenTemporarioService` criado no plano de convite. Este plano implementa gestão administrativa completa com log de auditoria (append-only).

---

## Arquivos a Criar

### Migration

`src/main/resources/db/migration/V12__create_logs_auditoria.sql`
```sql
CREATE TABLE logs_auditoria (
  id BIGSERIAL PRIMARY KEY,
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  usuario_id UUID REFERENCES usuarios(id),
  acao VARCHAR(100) NOT NULL,
  entidade VARCHAR(50) NOT NULL,
  entidade_id VARCHAR(100),
  dados_anteriores JSONB,
  dados_novos JSONB,
  motivo TEXT,
  ip VARCHAR(45),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_escola_criado ON logs_auditoria(escola_id, criado_em DESC);
```

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/response/AdminDashboardResponse.java`
- KPIs: totalAlunos, totalProfessores, totalTurmas, frequenciaMedia, mediaNotas
- desempenhoPorSerie: lista de serie com média
- alertasConsolidados: lista de alertas
- atividadeRecente: últimas ações relevantes

`src/main/java/br/com/inovadados/teacherplatform/dto/request/ConvidarProfessorRequest.java`
```java
public record ConvidarProfessorRequest(
  @NotBlank String nome,
  @Email @NotBlank String email,
  @NotEmpty List<String> disciplinas
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/CriarAlunoRequest.java`
```java
public record CriarAlunoRequest(
  @NotBlank String nome,
  @Email @NotBlank String email,
  @NotEmpty List<Long> turmasIds,
  List<ResponsavelDto> responsaveis
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/AlterarStatusProfessorRequest.java`
```java
public record AlterarStatusProfessorRequest(boolean ativo, @NotBlank String motivo) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/TransferirAlunoRequest.java`
```java
public record TransferirAlunoRequest(@NotNull Long novaTurmaId) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/ConfiguracoesEscolaRequest.java`
- Dados da escola, nota mínima, frequência mínima, sistema de avaliação, configurações de comunicação

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/AuditoriaService.java`
- `registrar(UUID usuarioId, String acao, String entidade, String entidadeId, Object dadosAnteriores, Object dadosNovos, String motivo, HttpServletRequest request)` → sempre INSERT, nunca UPDATE/DELETE
- Converter `dadosAnteriores` e `dadosNovos` para JSONB via Jackson ObjectMapper
- Utilizar via `@Autowired` em todos os serviços admin que executam ações destrutivas

`src/main/java/br/com/inovadados/teacherplatform/service/AdminService.java`
- `getDashboard(Long escolaId)` → agrega KPIs usando queries nativas otimizadas
- `listarProfessores(Long escolaId, String nome, String status, Pageable pageable)` → lista paginada
- `convidarProfessor(ConvidarProfessorRequest req, Long escolaId)` → cria usuário inativo + token convite + dispara e-mail
- `alterarStatusProfessor(UUID professorId, AlterarStatusProfessorRequest req, UUID adminId)` → atualiza `ativo`; registra auditoria com `dados_anteriores` e `motivo`
- `importarProfessores(MultipartFile file, Long escolaId)` → reutiliza `PlanilhaParserService`
- `listarAlunos(Long escolaId, String filtros, Pageable pageable)`
- `criarAluno(CriarAlunoRequest req, Long escolaId, UUID adminId)` → cria usuário + matrículas + vínculos responsáveis + convites
- `transferirAluno(UUID alunoId, TransferirAlunoRequest req, UUID adminId)` → soft delete matrícula antiga; nova matrícula; auditoria
- `importarAlunos(MultipartFile file, Long escolaId)`
- `getConfiguracoes(Long escolaId)` → retorna configurações da escola
- `atualizarConfiguracoes(Long escolaId, ConfiguracoesEscolaRequest req, UUID adminId)` → atualiza e registra auditoria

`src/main/java/br/com/inovadados/teacherplatform/service/EmailService.java`
- `enviarConviteProfessor(String email, String nome, String linkConvite)` → placeholder com log (integrar SMTP/SES no futuro)
- `enviarConviteAluno(String email, String nome, String linkConvite)` → idem

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/AdminController.java`
- `@RestController @RequestMapping("/admin")`
- `GET /dashboard`
- `GET /professores` (paginado + filtros)
- `POST /professores/convidar`
- `PATCH /professores/:id/status`
- `POST /professores/importar`
- `GET /alunos`
- `POST /alunos`
- `PATCH /alunos/:id/turma`
- `POST /alunos/importar`
- `GET /escola/configuracoes`
- `PUT /escola/configuracoes`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `SecurityConfig` | `/admin/**` somente ADMIN; COORDENADOR tem acesso a GET mas não a POST/PATCH/PUT/DELETE |
| `pom.xml` | Nada novo — poi-ooxml já adicionado em plano anterior |

---

## Ordem de Implementação

```
1. Migration V12 — logs_auditoria
2. DTOs de request e response
3. AuditoriaService (independente, testar serialização JSON)
4. EmailService (stub com log)
5. AdminService — getDashboard, listarProfessores (read-only primeiro)
6. AdminService — convidarProfessor, alterarStatus, importarProfessores
7. AdminService — listarAlunos, criarAluno, transferirAluno, importarAlunos
8. AdminService — configuracoes
9. AdminController
10. SecurityConfig — regras ADMIN vs COORDENADOR
11. Testes unitários: AuditoriaService (serialização), AdminService com mocks
12. Testes de integração: ciclo convidar → ativar → desativar com auditoria
```

---

## Resumo

- **12 arquivos** a criar (DTOs, 3 serviços, 1 controller, 1 migration)
- **1 arquivo** a modificar (SecurityConfig)
- **Nenhuma dependência nova** no pom.xml
- **Complexidade mantida:** G
