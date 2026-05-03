# Plano de Implementação — backend-dashboard_endpoint-professor

> **Task origem:** `docs/Tasks/backend-dashboard_endpoint-professor.md`
> **Escopo:** Backend — Dashboard e Relatórios
> **Complexidade:** G
> **Sprint:** 5 — Dashboards
> **Depende de:** `backend-model_avaliacoes-plan.md`, `backend-frequencia_endpoint-lancar-plan.md`

---

## Contexto do Codebase

Todas as entidades (Avaliacao, Entrega, Frequencia, Usuario, Turma, Questao) já existem com dados. Spring Security, JWT configurados. Nenhuma biblioteca de geração de PDF está presente ainda. Este plano implementa endpoints de aggregação de dados e geração assíncrona de relatórios PDF.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- Geração de PDF -->
<dependency>
  <groupId>com.itextpdf</groupId>
  <artifactId>itext-core</artifactId>
  <version>9.1.0</version>
  <type>pom</type>
</dependency>

<!-- Fila assíncrona de relatórios (usar Spring @Async por ora) -->
<!-- RabbitMQ pode ser adicionado futuramente para escala -->
```

### Configurações em application.properties
```properties
# S3 ou storage local para PDFs gerados
app.storage.pdf-base-url=http://localhost:8080/relatorios
app.storage.pdf-dir=./uploads/relatorios
app.relatorio.url-validade-horas=24
```

---

## Arquivos a Criar

### DTOs de Dashboard

`src/main/java/br/com/inovadados/teacherplatform/dto/response/DashboardProfessorResponse.java`
```java
public record DashboardProfessorResponse(
  List<AlertaDashboardDto> alertas,
  List<TurmaDashboardDto> turmas,
  List<MediaHistoricaDto> mediasHistoricas,
  List<MapaCalorDto> mapaCalor
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/AlertaDashboardDto.java`
```java
public record AlertaDashboardDto(
  String tipo, // "RISCO_REPROVACAO" | "ATIVIDADE_CORRIGIR" | "GABARITO_PENDENTE"
  String descricao,
  Long referenciaId,
  String referenciaUrl
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/DesempenhoTurmaResponse.java`
- Métricas: média, maior/menor nota, % aprovação, % frequência
- Histograma: `List<FaixaNotaDto>` (faixa 0-2, 2-4, 4-6, 6-8, 8-10 com lista de alunos)
- Ranking de alunos com tendência (↑↓)
- Linha do tempo de avaliações

`src/main/java/br/com/inovadados/teacherplatform/dto/response/DesempenhoAlunoResponse.java`
- Situação: "APROVADO_EM_ANDAMENTO" | "EM_RISCO" | "REPROVADO_POR_FALTA"
- Evolução de notas por avaliação
- Frequência mensal
- Tópicos por % de acerto
- Avaliações com posição na turma

`src/main/java/br/com/inovadados/teacherplatform/dto/request/ObservacaoRequest.java`
```java
public record ObservacaoRequest(@NotBlank String texto) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/RelatorioStatusResponse.java`
```java
public record RelatorioStatusResponse(String jobId, String status, String urlDownload) {}
```

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/DashboardService.java`
- `getDashboardProfessor(UUID professorId)` → agrega alertas, métricas por turma, médias históricas, mapa de calor
- `getDesempenhoTurma(Long turmaId, String periodo, UUID usuarioId)` → verifica permissão; calcula histograma, ranking, tendência (comparar médias das últimas 3 vs 3 anteriores)
- `getDesempenhoAluno(Long turmaId, UUID alunoId, UUID usuarioId, PerfilEnum perfil)` → determina situação; calcula frequência mensal; identifica tópicos fracos
- `calcularTendencia(List<BigDecimal> notas)` → `"UP" | "DOWN" | "STABLE"`
- `calcularPosicaoNaTurma(Long avaliacaoId, UUID alunoId)` → rank por nota

`src/main/java/br/com/inovadados/teacherplatform/service/ObservacaoService.java`
- `criar(Long turmaId, UUID alunoId, UUID professorId, ObservacaoRequest req)` → salva em `observacoes_professor`
- `listar(Long turmaId, UUID alunoId, PerfilEnum perfil)` → se `perfil == RESPONSAVEL` → retorna lista vazia (dados privados)

`src/main/java/br/com/inovadados/teacherplatform/service/RelatorioService.java`
- `@Async solicitarRelatorioPDF(String tipo, Long referenciaId, UUID solicitanteId)` → gera PDF via iText, salva em disco com nome único, cria URL assinada com validade 24h
- `gerarPDFTurma(Long turmaId)` → usa `DashboardService.getDesempenhoTurma()`, monta PDF
- `gerarPDFAluno(Long turmaId, UUID alunoId)` → idem para perfil do aluno
- Retorna `jobId` imediatamente; quando concluído, cria notificação via evento

### Migration

`src/main/resources/db/migration/V9__create_observacoes_professor.sql`
```sql
CREATE TABLE observacoes_professor (
  id BIGSERIAL PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id UUID NOT NULL REFERENCES usuarios(id),
  professor_id UUID NOT NULL REFERENCES usuarios(id),
  texto TEXT NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_obs_turma_aluno ON observacoes_professor(turma_id, aluno_id);
```

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/DashboardController.java`
- `GET /professor/dashboard`
- `GET /turmas/:id/desempenho?periodo=:p`
- `GET /turmas/:id/alunos/:alunoId/desempenho`
- `POST /turmas/:id/alunos/:alunoId/observacoes`
- `GET /turmas/:id/alunos/:alunoId/observacoes`
- `POST /relatorios/turma/:id/pdf` → retorna `{ jobId, status: "PROCESSANDO" }`
- `POST /relatorios/aluno/:id/pdf`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `pom.xml` | Adicionar itext-core |
| `application.properties` | Adicionar app.storage.*, app.relatorio.* |
| `SecurityConfig` | `/relatorios/**` somente PROFESSOR e ADMIN; `/turmas/:id/alunos/:alunoId/desempenho` visível para RESPONSAVEL (com filtro no service) |

---

## Ordem de Implementação

```
1. pom.xml — iText
2. application.properties — storage
3. Migration V9 — observacoes_professor
4. DTOs de request e response
5. DashboardService — calcularTendencia e calcularPosicao (funções puras, testar primeiro)
6. DashboardService — getDashboardProfessor, getDesempenhoTurma, getDesempenhoAluno
7. ObservacaoService
8. RelatorioService — geração de PDF (integrar iText)
9. DashboardController
10. SecurityConfig — novas rotas
11. Testes unitários: calcularTendencia, calcularPosicao
12. Testes de integração: GET /professor/dashboard com dados reais
```

---

## Resumo

- **12 arquivos** a criar (DTOs, 3 serviços, 1 controller, 1 migration)
- **3 arquivos** a modificar (pom.xml, application.properties, SecurityConfig)
- **Biblioteca a adicionar:** itext-core
- **Complexidade mantida:** G
