# Testes Unitários — FrequenciaService: Faltas Consecutivas e Percentual de Presença

> **Escopo:** Backend — `FrequenciaService`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`FrequenciaService` registra e consulta a frequência de alunos. Dois algoritmos são críticos: a detecção de 3 faltas consecutivas (que dispara alertas para responsáveis) e o cálculo do percentual de presença (que determina aprovação/reprovação). Erros nesses cálculos geram alertas falsos ou deixam alunos em risco sem notificação.

---

## O que deve ser implementado

Criar `FrequenciaServiceTest` com foco nas duas lógicas centrais. `verificarTresFaltasConsecutivas` é private — testado indiretamente via `lancarFrequencia` (verificando se `alertaFrequenciaService.verificarAlertas` é chamado com o boolean correto). O percentual é testado via `buscarHistorico`.

**Parte 1 — Faltas consecutivas (8 cenários):**
- Sem faltas, duas faltas, exatamente três, mais de três
- Três intercaladas (não consecutivas), três no meio, lista vazia
- Duas sequências de dois (não ativam)

**Parte 2 — Percentual e cálculo de faltas em `buscarHistorico` (7 cenários):**
- 100%, 75%, 50%, 0%, divisão por zero (0 aulas), arredondamento 66.66%, 33.33%

**Parte 3 — `lancarFrequencia` (7 cenários):**
- Turma não encontrada, usuário não encontrado, aluno não encontrado
- Novo registro: `lancadoEm` preenchido
- Registro existente: `editadoEm` atualizado
- Alerta disparado para cada aluno

---

## Critérios de Aceite

- [ ] `[AUSENTE, AUSENTE, PRESENTE]` → não consecutivas (false)
- [ ] `[AUSENTE, AUSENTE, AUSENTE]` → 3 consecutivas (true)
- [ ] `[AUSENTE, PRESENTE, AUSENTE, AUSENTE]` → não consecutivas (false)
- [ ] `[PRESENTE, AUSENTE, AUSENTE, AUSENTE, PRESENTE]` → 3 consecutivas (true)
- [ ] `[AUSENTE, AUSENTE, AUSENTE, AUSENTE]` → 3+ consecutivas (true)
- [ ] `[]` → false (lista vazia)
- [ ] `[AUSENTE, AUSENTE, PRESENTE, AUSENTE, AUSENTE]` → false
- [ ] Percentual 10/10 aulas = 100.0
- [ ] Percentual 3/4 aulas = 75.0
- [ ] Percentual 0/5 aulas = 0.0
- [ ] Percentual com 0 aulas = 0.0 (sem divisão por zero)
- [ ] Percentual 2/3 aulas = 66.7 (arredondamento 1 casa)
- [ ] Faltas = `registros.size() - presenças`
- [ ] Turma não encontrada → `TurmaNaoEncontradaException`
- [ ] Usuário lançador não encontrado → `UnauthorizedException`
- [ ] Aluno não encontrado → `IllegalArgumentException`
- [ ] Novo registro: `lancadoEm` preenchido, `editadoEm = null`
- [ ] Registro existente: `editadoEm` atualizado, `lancadoEm` preservado
- [ ] `alertaFrequenciaService.verificarAlertas` chamado uma vez por aluno no request

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/FrequenciaService.md`
- **Seções:** `verificarTresFaltasConsecutivas`, `buscarHistorico`, `lancarFrequencia`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/FrequenciaServiceTest.java`

**Para testar consecutivas indiretamente:**
```java
// Mock: countAulasByTurmaId retorna 5, countByTurmaIdAndAlunoIdAndStatus retorna 2
// histórico com [AUSENTE, AUSENTE, AUSENTE] → verificarAlertas chamado com tresFaltasConsecutivas=true
```

**Fórmula do percentual (atenção):** O denominador é `totalAulas` (contagem de todas as aulas da turma via repositório), não `registros.size()`.

---

## Notas e Edge Cases

- Três faltas **consecutivas** — não três no total
- `buscarHistorico` usa `countAulasByTurmaId` (todas as aulas da turma) como denominador
- `lancarFrequencia` faz upsert: cria ou atualiza por `(turmaId, alunoId, data)`

---

## Definition of Done

- [ ] Classe `FrequenciaServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 22 testes)
- [ ] Testes passam com `./mvnw test -Dtest=FrequenciaServiceTest`
