# Testes Unitários — DashboardService: Tendência, Posição e Situação

> **Escopo:** Backend — `DashboardService` (métodos package-private e lógica de negócio)  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`DashboardService` agrega métricas de desempenho de turmas e alunos. Três métodos package-private concentram a lógica mais testável: `calcularTendencia` (UP/DOWN/STABLE), `calcularPosicaoNaTurma` (ranking) e `determinarSituacao` (situação acadêmica). Erros nesses métodos geram dashboards enganosos para professores e alunos.

---

## O que deve ser implementado

Criar `DashboardServiceTest` com testes diretos dos métodos package-private e testes de controle de acesso e cálculo de métricas via `getDesempenhoTurma`. Os cálculos de aprovação e frequência também devem ser cobertos.

---

## Critérios de Aceite

**`calcularTendencia`:**
- [ ] Lista vazia → `"STABLE"`
- [ ] Um elemento → `"STABLE"`
- [ ] Média recente - anterior > 0.3 → `"UP"`
- [ ] Média recente - anterior < -0.3 → `"DOWN"`
- [ ] Diferença dentro de ±0.3 → `"STABLE"`
- [ ] Diferença exatamente +0.3 → `"STABLE"` (não > 0.3)
- [ ] Diferença exatamente -0.3 → `"STABLE"` (não < -0.3)
- [ ] Lista com número ímpar: metade recente tem o elemento central

**`calcularPosicaoNaTurma`:**
- [ ] Único aluno com nota → posição 1
- [ ] Aluno com maior nota → posição 1
- [ ] Aluno com menor nota → última posição
- [ ] Aluno sem nota (filtrado) → `size + 1`
- [ ] Aluno não participou → `size + 1`

**`determinarSituacao`:**
- [ ] Frequência < 75% → `"REPROVADO_POR_FALTA"` (independente da nota)
- [ ] Frequência = 75% → não reprovado por falta
- [ ] Frequência ok, média < 5 → `"EM_RISCO"`
- [ ] Frequência ok, média = 5.0 → `"APROVADO_EM_ANDAMENTO"`
- [ ] Frequência ok, média > 5 → `"APROVADO_EM_ANDAMENTO"`

**`getDesempenhoTurma` — controle de acesso:**
- [ ] Turma não encontrada → `TurmaNaoEncontradaException`
- [ ] Professor acessa sua turma → ok
- [ ] Professor acessa turma alheia → `AcessoNegadoException`
- [ ] ADMIN acessa qualquer turma → ok
- [ ] COORDENADOR acessa qualquer turma → ok

**`getDesempenhoTurma` — cálculo de aprovação:**
- [ ] Média aluno ≥ 5.0 → aprovado
- [ ] 3 de 4 alunos aprovados → pctAprovacao = 75.0

**`getDesempenhoTurma` — cálculo de frequência:**
- [ ] Fórmula: `totalPresencas / (matriculas.size * totalAulas) * 100`
- [ ] Sem matrículas → 100.0
- [ ] Sem aulas → 100.0

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/DashboardService.md`
- **Seções:** `calcularTendencia`, `calcularPosicaoNaTurma`, `determinarSituacao`, `getDesempenhoTurma`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/DashboardServiceTest.java`

**Acesso direto aos métodos package-private:**
```java
String tendencia = dashboardService.calcularTendencia(List.of(new BigDecimal("5"), new BigDecimal("8")));
int posicao = dashboardService.calcularPosicaoNaTurma(avaliacaoId, alunoId);
```

---

## Notas e Edge Cases

- `calcularTendencia` com lista ímpar: `metade = size / 2`, e a segunda metade vai de `metade` até o final — o elemento central pertence à segunda metade
- `calcularPosicaoNaTurma` ordena por nota decrescente — empates preservam a ordem original da lista
- Frequência usa denominador `matriculas.size * totalAulas`, não apenas `totalPresencas`

---

## Definition of Done

- [ ] Classe `DashboardServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 25 testes)
- [ ] Testes passam com `./mvnw test -Dtest=DashboardServiceTest`
