# Spec de Testes Unitários — DashboardService

**Classe:** `br.com.inovadados.teacherplatform.service.DashboardService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/DashboardService.java`

---

## Visão Geral

`DashboardService` agrega métricas de turmas, alunos e frequência. Os métodos mais críticos para teste unitário são `calcularTendencia`, `calcularPosicaoNaTurma` e `determinarSituacao`, que possuem lógica pura sem efeitos colaterais.

**Dependências para mock:** `TurmaRepository`, `MatriculaRepository`, `AvaliacaoRepository`, `EntregaRepository`, `RegistroFrequenciaRepository`, `RespostaRepository`, `UsuarioRepository`.

---

## Método: `calcularTendencia(List<BigDecimal> notas)` (package-private)

**Regra:** Divide as notas em duas metades. Compara a média da metade recente com a anterior.

| # | Cenário | Notas | Resultado |
|---|---------|-------|-----------|
| 1 | Lista vazia | `[]` | `"STABLE"` |
| 2 | Uma única nota | `[8.0]` | `"STABLE"` |
| 3 | Crescimento > 0.3 | `[5.0, 5.0, 8.0, 8.0]` — média recente 8.0, anterior 5.0 | `"UP"` |
| 4 | Queda < -0.3 | `[8.0, 8.0, 5.0, 5.0]` — diferença -3.0 | `"DOWN"` |
| 5 | Variação dentro de ±0.3 | `[7.0, 7.2]` — diferença 0.2 | `"STABLE"` |
| 6 | Exatamente +0.3 (borda) | diferença = 0.3 | `"STABLE"` (não > 0.3) |
| 7 | Exatamente -0.3 (borda) | diferença = -0.3 | `"STABLE"` (não < -0.3) |
| 8 | Número ímpar de notas | `[5.0, 7.0, 9.0]` — metade=1, recente=[7.0,9.0] avg=8.0, anterior=[5.0] avg=5.0 | `"UP"` |

---

## Método: `calcularPosicaoNaTurma(Long avaliacaoId, UUID alunoId)` (package-private)

**Regra:** Ordena entregas por nota decrescente. Retorna a posição (1-based) do aluno. Se não encontrado, retorna `size + 1`.

| # | Cenário | Dados | Posição Esperada |
|---|---------|-------|-----------------|
| 1 | Único aluno com nota | 1 entrega com nota | `1` |
| 2 | Melhor nota | notas [8.0, 7.0, 6.0], alunoId = dono do 8.0 | `1` |
| 3 | Pior nota | notas [8.0, 7.0, 6.0], alunoId = dono do 6.0 | `3` |
| 4 | Aluno sem nota na lista | `notaFinal = null` — filtrado | posição = total_com_nota + 1 |
| 5 | Aluno não participou | alunoId não está em nenhuma entrega | `size + 1` |
| 6 | Notas empatadas | `[8.0, 8.0, 6.0]`, alunoId = segunda ocorrência de 8.0 | `2` |

---

## Método: `determinarSituacao(BigDecimal media, double pctFrequencia)` (private — testado via `getDesempenhoAluno`)

| # | Cenário | Média | Frequência | Situação Esperada |
|---|---------|-------|-----------|-------------------|
| 1 | Frequência baixa — reprovado por falta | qualquer | `74.9%` | `"REPROVADO_POR_FALTA"` |
| 2 | Frequência exatamente 75% | qualquer | `75.0%` | não é reprovado por falta |
| 3 | Freq ok, nota < 5 | `4.9` | `80%` | `"EM_RISCO"` |
| 4 | Freq ok, nota = 5.0 | `5.0` | `80%` | `"APROVADO_EM_ANDAMENTO"` |
| 5 | Freq ok, nota alta | `9.0` | `90%` | `"APROVADO_EM_ANDAMENTO"` |
| 6 | Freq baixa prevalece sobre nota | `9.0` | `50%` | `"REPROVADO_POR_FALTA"` |

---

## Método: `getDesempenhoTurma(Long turmaId, UUID usuarioId)`

### Controle de acesso

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Turma não encontrada | `turmaRepository.findById` retorna empty | lança `TurmaNaoEncontradaException` |
| 2 | Professor acessa sua turma | `turma.professor.id == usuarioId` | ok |
| 3 | Professor acessa turma alheia | `turma.professor.id ≠ usuarioId`, não é admin | lança `AcessoNegadoException` |
| 4 | ADMIN acessa qualquer turma | `perfil = ADMIN` | ok sem verificar professor |
| 5 | COORDENADOR acessa qualquer turma | `perfil = COORDENADOR` | ok sem verificar professor |

### Cálculo de aprovação

**Critério:** média do aluno >= 5.0

| # | Alunos | Médias | % Aprovação Esperada |
|---|--------|--------|----------------------|
| 6 | 4 alunos | [7.0, 3.0, 8.0, 5.0] | `75.0` (3 de 4) |
| 7 | 0 alunos | nenhuma matrícula | `0.0` |
| 8 | Todos aprovados | [6.0, 7.0, 8.0] | `100.0` |

### Cálculo de frequência da turma

**Fórmula:** `totalPresencas / (totalMatriculas * totalAulas) * 100`

| # | Matrículas | Total Aulas | Total Presenças | % Esperado |
|---|-----------|-------------|-----------------|------------|
| 9 | 2 alunos | 10 aulas | 15 presenças | `75.0` |
| 10 | 0 alunos | qualquer | qualquer | `100.0` |
| 11 | qualquer | 0 aulas | qualquer | `100.0` |

---

## Método: `calcularHistograma` (private — testado via `getDesempenhoTurma`)

**Faixas:** `[0-2, 2-4, 4-6, 6-8, 8-10]`

| # | Cenário | Médias | Faixa "8-10" |
|---|---------|--------|-------------|
| 1 | Nota 10.0 na última faixa | `[10.0]` | 1 aluno (inclusivo em ambas as bordas) |
| 2 | Nota 8.0 na faixa 8-10 | `[8.0]` | 1 aluno |
| 3 | Nota 7.9 na faixa 6-8 | `[7.9]` | 0 na faixa 8-10 |
| 4 | Nota 6.0 na borda | `[6.0]` | faixa "6-8", não "4-6" |

---

## Regras de Negócio Críticas

- `calcularTendencia` divide a lista em duas metades — se ímpar, a última metade tem o elemento central.
- Frequência da turma usa denominador `totalMatriculas * totalAulas` (cross product), não apenas o total de registros.
- Alunos com frequência < 75% são `REPROVADO_POR_FALTA` independentemente da nota.
- Aprovação é definida por média >= 5.0 por aluno, não por nota individual de cada avaliação.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock TurmaRepository turmaRepository;
    @Mock MatriculaRepository matriculaRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock RegistroFrequenciaRepository frequenciaRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks DashboardService dashboardService;
}
```
