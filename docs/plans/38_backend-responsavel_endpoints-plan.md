# Plano de Implementação — backend-responsavel_endpoints

> **Task origem:** `docs/Tasks/backend-responsavel_endpoints.md`
> **Escopo:** Backend — Área do Responsável
> **Complexidade:** M
> **Sprint:** 6 — Responsável e Administração
> **Depende de:** `backend-dashboard_endpoint-professor-plan.md` (DashboardService)

---

## Contexto do Codebase

`DashboardService` já existe com `getDesempenhoAluno()`. Entidade `Responsavel` vincula `Usuario (responsavel)` a `Usuario (aluno)` via tabela `responsaveis_alunos` (definida no modelo de turmas). Spring Security com JWT configurado. Este plano reutiliza amplamente o DashboardService com filtro de permissão por vínculo responsável-aluno.

---

## Arquivos a Criar

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/response/PainelResponsavelResponse.java`
```java
public record PainelResponsavelResponse(
  AlunoResumoDto aluno,
  BigDecimal mediaGeral,
  double percentualFrequencia,
  AvaliacaoProximaDto proximaProva,
  List<AlertaDto> alertasAtivos
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/BoletimResponse.java`
```java
public record BoletimResponse(
  String periodo,
  List<BoletimDisciplinaDto> disciplinas
) {}
public record BoletimDisciplinaDto(
  String disciplina,
  BigDecimal notaBimestre1,
  BigDecimal notaBimestre2,
  BigDecimal notaBimestre3,
  BigDecimal notaBimestre4,
  BigDecimal mediaFinal,
  String situacao // "APROVADO" | "RECUPERACAO" | "REPROVADO" | "EM_ANDAMENTO"
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/FrequenciaResponsavelResponse.java`
- Resumo numérico + calendário mensal com status + lista de faltas

`src/main/java/br/com/inovadados/teacherplatform/dto/response/CalendarioResponsavelResponse.java`
- Provas futuras + histórico com notas

### Serviço

`src/main/java/br/com/inovadados/teacherplatform/service/ResponsavelService.java`
- `verificarVinculo(UUID responsavelId, UUID alunoId)` → busca em `responsaveis_alunos`; lança `AcessoNegadoException` se não vinculado
- `listarAlunos(UUID responsavelId)` → lista filhos vinculados
- `getPainel(UUID responsavelId, UUID alunoId)` → chama `verificarVinculo`; agrega média, frequência, próxima prova, alertas
- `getBoletim(UUID responsavelId, UUID alunoId, String periodo)` → chama `verificarVinculo`; busca notas sem expor observações privadas do professor (filtro explícito: nunca retornar campo `observacoes`)
- `getFrequencia(UUID responsavelId, UUID alunoId)` → chama `verificarVinculo`; reutiliza lógica de `FrequenciaService`
- `getCalendario(UUID responsavelId, UUID alunoId)` → provas publicadas das turmas do aluno + resultados se gabarito disponível

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/ResponsavelController.java`
- `@RestController @RequestMapping("/responsavel")`
- `GET /alunos`
- `GET /alunos/:alunoId/painel`
- `GET /alunos/:alunoId/boletim?periodo=:p`
- `GET /alunos/:alunoId/frequencia`
- `GET /alunos/:alunoId/calendario`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `SecurityConfig` | Rotas `/responsavel/**` somente perfil RESPONSAVEL |

---

## Ordem de Implementação

```
1. DTOs de response
2. ResponsavelService — verificarVinculo (base para todos os métodos)
3. ResponsavelService — listarAlunos, getPainel, getBoletim, getFrequencia, getCalendario
4. ResponsavelController
5. SecurityConfig
6. Testes unitários: verificarVinculo (responsável correto e incorreto)
7. Testes de integração: GET /responsavel/alunos/:id/painel
```

---

## Resumo

- **6 arquivos** a criar (DTOs, 1 serviço, 1 controller)
- **1 arquivo** a modificar (SecurityConfig)
- **Nenhuma dependência nova** no pom.xml
- **Complexidade mantida:** M
