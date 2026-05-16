# Spec de Testes Unitários — FlashcardService

**Classe:** `br.com.inovadados.teacherplatform.service.FlashcardService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/FlashcardService.java`

---

## Visão Geral

`FlashcardService` implementa o algoritmo **SM-2** de repetição espaçada para revisão de flashcards. O núcleo testável é `aplicarSm2` (package-private via reflexão ou método extraído), que atualiza o fator de facilidade e o intervalo de revisão.

**Dependências para mock:** `FlashcardRepository`, `FlashcardEstadoSm2Repository`, `ProgressoFlashcardRepository`, `MatriculaRepository`, `UsuarioRepository`.

---

## Algoritmo SM-2 — Regras a Testar

| Situação | Fator de Facilidade | Intervalo de Dias | Próxima Revisão |
|----------|--------------------|--------------------|-----------------|
| Sabia (`sabia = true`) | `fator + 0.1` | `max(1, round(intervalo * novoFator))` | `hoje + novoIntervalo` |
| Não sabia (`sabia = false`) | `max(fator - 0.2, 1.3)` | `1` (sempre resetado) | `hoje + 1` |
| Fator cai abaixo de 1.3 | fixado em `1.3` | `1` | `hoje + 1` |

**Estado inicial padrão:** `fatorFacilidade = 2.5`, `intervaloDias = 1` (valores default da entidade).

---

## Método: `aplicarSm2` (testado via `registrarAvaliacao`)

### Cenários — `sabia = true`

| # | Estado Inicial | Entrada | Fator Esperado | Intervalo Esperado |
|---|---------------|---------|---------------|-------------------|
| 1 | fator=2.5, intervalo=1 | `sabia=true` | 2.6 | `max(1, round(1 * 2.6)) = 3` |
| 2 | fator=2.5, intervalo=3 | `sabia=true` | 2.6 | `round(3 * 2.6) = 8` |
| 3 | fator=2.5, intervalo=8 | `sabia=true` | 2.6 | `round(8 * 2.6) = 21` |
| 4 | fator=3.0, intervalo=10 | `sabia=true` | 3.1 | `round(10 * 3.1) = 31` |

### Cenários — `sabia = false`

| # | Estado Inicial | Entrada | Fator Esperado | Intervalo Esperado |
|---|---------------|---------|---------------|-------------------|
| 5 | fator=2.5, intervalo=10 | `sabia=false` | 2.3 | 1 |
| 6 | fator=1.5, intervalo=5 | `sabia=false` | 1.3 (mínimo) | 1 |
| 7 | fator=1.3, intervalo=3 | `sabia=false` | 1.3 (fixado) | 1 |
| 8 | fator=1.4, intervalo=7 | `sabia=false` | 1.3 (mínimo) | 1 |

### Cenários — `totalRevisoes`

| # | Estado Inicial | Entrada | Total Revisões Esperado |
|---|---------------|---------|------------------------|
| 9 | `totalRevisoes=0` | qualquer | 1 |
| 10 | `totalRevisoes=5` | qualquer | 6 |

### Cenário — `proximaRevisao`

| # | Entrada | Próxima Revisão Esperada |
|---|---------|--------------------------|
| 11 | `sabia=true`, intervalo calculado=8 | `LocalDate.now().plusDays(8)` |
| 12 | `sabia=false` | `LocalDate.now().plusDays(1)` |

---

## Método: `registrarAvaliacao(UUID alunoId, Long flashcardId, boolean sabia)`

| # | Cenário | Comportamento do Mock | Saída Esperada |
|---|---------|----------------------|----------------|
| 1 | Flashcard não encontrado | `flashcardRepository.findById` retorna empty | lança `UnauthorizedException` |
| 2 | Estado SM-2 não existe — cria novo | `estadoSm2Repository.findBy...` retorna empty | cria estado com `alunoId` e `flashcard` corretos |
| 3 | Estado SM-2 já existe — atualiza | `estadoSm2Repository.findBy...` retorna estado existente | atualiza o mesmo objeto |
| 4 | Salva estado após aplicar SM-2 | qualquer | `estadoSm2Repository.save()` chamado 1 vez |
| 5 | Registra histórico | `sabia=true` | `progressoRepository.save()` chamado com `resultado=FACIL` |
| 6 | Registra histórico | `sabia=false` | `progressoRepository.save()` chamado com `resultado=DIFICIL` |
| 7 | Aluno não encontrado no histórico | `usuarioRepository.findById` retorna empty | lança `UnauthorizedException` |

---

## Método: `getFlashcardsPriorizados(UUID alunoId, Long turmaId)`

| # | Cenário | Comportamento do Mock | Saída Esperada |
|---|---------|----------------------|----------------|
| 1 | `turmaId` informado — filtra por turma | estados de turmas diferentes | retorna apenas flashcards da turma informada |
| 2 | `turmaId = null` — usa todas as turmas do aluno | `matriculaRepository` retorna turmas [1L, 2L] | retorna flashcards das turmas 1 e 2 |
| 3 | Nenhum estado com revisão pendente | `estadoSm2Repository` retorna lista vazia | retorna lista vazia |
| 4 | Retorna ordenado por `proximaRevisao` ASC | estados com datas diferentes | ordem crescente de data |

---

## Regras de Negócio Críticas

- O fator mínimo é **1.3** — nunca deve ficar abaixo disso.
- Ao errar, o intervalo volta sempre para **1 dia**, independente do histórico.
- `totalRevisoes` é incrementado em **1** a cada avaliação, independente do resultado.
- `getFlashcardsPriorizados` filtra por `proximaRevisao <= hoje`.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock FlashcardRepository flashcardRepository;
    @Mock FlashcardEstadoSm2Repository estadoSm2Repository;
    @Mock ProgressoFlashcardRepository progressoRepository;
    @Mock MatriculaRepository matriculaRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks FlashcardService flashcardService;

    private FlashcardEstadoSm2 estadoComFator(double fator, int intervalo) {
        var e = new FlashcardEstadoSm2();
        e.setFatorFacilidade(new BigDecimal(fator));
        e.setIntervaloDias(intervalo);
        e.setTotalRevisoes(0);
        return e;
    }
}
```
