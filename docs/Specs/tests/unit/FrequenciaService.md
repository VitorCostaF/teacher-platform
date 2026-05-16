# Spec de Testes Unitários — FrequenciaService

**Classe:** `br.com.inovadados.teacherplatform.service.FrequenciaService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/FrequenciaService.java`

---

## Visão Geral

`FrequenciaService` gerencia o lançamento, edição e consulta de frequência de alunos. Os pontos mais críticos são o cálculo do percentual de presença e a detecção de 3 faltas consecutivas.

**Dependências para mock:** `RegistroFrequenciaRepository`, `UsuarioRepository`, `TurmaRepository`, `AlertaFrequenciaService`.

---

## Método: `verificarTresFaltasConsecutivas(List<RegistroFrequencia>)` (private — testado via `lancarFrequencia`)

| # | Cenário | Sequência de Status | Resultado |
|---|---------|---------------------|-----------|
| 1 | Sem faltas | [PRESENTE, PRESENTE, PRESENTE] | `false` |
| 2 | Duas faltas consecutivas | [AUSENTE, AUSENTE, PRESENTE] | `false` |
| 3 | Exatamente três faltas consecutivas | [AUSENTE, AUSENTE, AUSENTE] | `true` |
| 4 | Três faltas intercaladas | [AUSENTE, PRESENTE, AUSENTE, AUSENTE] | `false` |
| 5 | Três faltas no meio | [PRESENTE, AUSENTE, AUSENTE, AUSENTE, PRESENTE] | `true` |
| 6 | Lista vazia | `[]` | `false` |
| 7 | Mais de três faltas consecutivas | [AUSENTE, AUSENTE, AUSENTE, AUSENTE] | `true` |
| 8 | Sequências separadas por presença | [AUSENTE, AUSENTE, PRESENTE, AUSENTE, AUSENTE] | `false` |

---

## Método: `buscarHistorico(Long turmaId, UUID alunoId)`

### Cálculo do percentual de presença

**Fórmula:** `(totalPresencas / totalAulas) * 100` arredondado para 1 casa decimal.

| # | Cenário | Total Aulas | Total Presenças | Percentual Esperado |
|---|---------|-------------|-----------------|---------------------|
| 1 | 100% de presença | 10 | 10 | `100.0` |
| 2 | 75% de presença | 4 | 3 | `75.0` |
| 3 | 50% de presença | 2 | 1 | `50.0` |
| 4 | 0% de presença | 5 | 0 | `0.0` |
| 5 | Sem aulas registradas | 0 | 0 | `0.0` (divisão por zero protegida) |
| 6 | Arredondamento — 66.66% | 3 | 2 | `66.7` |
| 7 | Arredondamento — 33.33% | 3 | 1 | `33.3` |

### Cálculo de faltas

**Fórmula:** `totalFaltas = registros.size() - totalPresencas`

| # | Registros do Aluno | Presenças | Faltas Esperadas |
|---|-------------------|-----------|-----------------|
| 1 | 5 registros | 3 | 2 |
| 2 | 5 registros | 5 | 0 |
| 3 | 0 registros | 0 | 0 |

### Mapeamento do calendário

| # | Cenário | Dados | Saída Esperada |
|---|---------|-------|----------------|
| 1 | Registro com observação | status=AUSENTE, obs="Viagem" | `DiaFrequenciaDto` com data, status e observação |
| 2 | Registro sem observação | status=PRESENTE, obs=null | `DiaFrequenciaDto` com obs=null |
| 3 | Ordenação por data | registros fora de ordem | `findByTurmaIdAndAlunoIdOrderByDataAulaAsc` garante a ordem |

---

## Método: `lancarFrequencia(Long turmaId, LancarFrequenciaRequest, UUID lancadoPorId)`

| # | Cenário | Comportamento do Mock | Saída Esperada |
|---|---------|----------------------|----------------|
| 1 | Turma não encontrada | `turmaRepository.findById` retorna empty | lança `TurmaNaoEncontradaException` |
| 2 | Usuário lançador não encontrado | `usuarioRepository.findById(lancadoPorId)` retorna empty | lança `UnauthorizedException` |
| 3 | Aluno não encontrado | `usuarioRepository.findById(alunoId)` retorna empty | lança `IllegalArgumentException` |
| 4 | Novo registro criado | `findByTurmaIdAndAlunoIdAndDataAula` retorna empty | `lancadoEm` preenchido, `editadoEm = null` |
| 5 | Registro existente atualizado | `findByTurmaIdAndAlunoIdAndDataAula` retorna registro | `editadoEm` atualizado, `lancadoEm` preservado |
| 6 | Alerta disparado para cada aluno | 3 alunos no request | `alertaFrequenciaService.verificarAlertas` chamado 3 vezes |
| 7 | Retorno correto | request com 2 alunos | `FrequenciaResponse` com data e lista de 2 alunos |

---

## Método: `editarFrequencia(Long turmaId, Long frequenciaId, LancarFrequenciaRequest, UUID lancadoPorId)`

| # | Cenário | Comportamento do Mock | Saída Esperada |
|---|---------|----------------------|----------------|
| 1 | Frequência de referência não encontrada | `findById(frequenciaId)` retorna empty | lança `IllegalArgumentException` |
| 2 | Registro do aluno não encontrado | `findByTurmaIdAndAlunoIdAndDataAula` retorna empty | lança `IllegalArgumentException` |
| 3 | `editadoEm` atualizado | qualquer | `editadoEm` != null no registro salvo |
| 4 | Data de aula preservada da referência | referência com `dataAula = 2024-03-01` | registros editados usam `2024-03-01` |

---

## Regras de Negócio Críticas

- Três faltas **consecutivas** acionam alerta — não três faltas no total.
- O percentual usa `totalAulas` (contagem de todas as aulas da turma) como denominador, não `registros.size()`.
- Faltas = `registros.size() - presenças`, não inclui aulas sem registro.
- Na edição, a data é extraída do registro de referência — o request não define a data.
- `lancarFrequencia` upsert: cria novo ou atualiza existente por `(turmaId, alunoId, data)`.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class FrequenciaServiceTest {

    @Mock RegistroFrequenciaRepository registroRepo;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock AlertaFrequenciaService alertaFrequenciaService;

    @InjectMocks FrequenciaService frequenciaService;

    private RegistroFrequencia registroFake(StatusFrequenciaEnum status) {
        var r = new RegistroFrequencia();
        r.setStatus(status);
        r.setDataAula(LocalDate.now());
        return r;
    }
}
```
