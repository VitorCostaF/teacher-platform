# Spec de Testes Unitários — GamificacaoService

**Classe:** `br.com.inovadados.teacherplatform.service.GamificacaoService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/GamificacaoService.java`

---

## Visão Geral

`GamificacaoService` calcula pontuação de alunos e verifica conquistas desbloqueadas com base em entregas e revisões de flashcards. As regras de pontuação e os critérios de conquistas são o núcleo a ser testado.

**Dependências para mock:** `EntregaRepository`, `FlashcardEstadoSm2Repository`, `ConquistaRepository`.

---

## Método: `calcularPontos(UUID alunoId)`

### Regras de pontuação

| Condição | Pontos |
|----------|--------|
| Entrega no prazo (`!entregaAtrasada`) com status ENTREGUE ou CORRIGIDA | +10 |
| Entrega atrasada (`entregaAtrasada`) com status ENTREGUE ou CORRIGIDA | +3 |
| Nota final >= 7.0 em qualquer entrega válida | +20 |
| Total de revisões de flashcards (soma de `totalRevisoes`) | +N (1 por revisão) |
| Entregas com status diferente de ENTREGUE/CORRIGIDA | 0 (ignoradas) |

### Cenários

| # | Cenário | Dados de Entrada | Pontos Esperados |
|---|---------|-----------------|-----------------|
| 1 | Nenhuma entrega, nenhum flashcard | listas vazias | `0` |
| 2 | 1 entrega no prazo, sem nota | `ENTREGUE`, `atrasada=false`, `nota=null` | `10` |
| 3 | 1 entrega no prazo, nota=8.0 | `ENTREGUE`, `atrasada=false`, `nota=8.0` | `30` (10+20) |
| 4 | 1 entrega atrasada, sem nota | `ENTREGUE`, `atrasada=true`, `nota=null` | `3` |
| 5 | 1 entrega atrasada, nota=9.0 | `ENTREGUE`, `atrasada=true`, `nota=9.0` | `23` (3+20) |
| 6 | 1 entrega com nota=6.9 (abaixo de 7) | `ENTREGUE`, `nota=6.9` | `10` (sem bônus de nota) |
| 7 | 1 entrega com nota=7.0 (exato) | `ENTREGUE`, `nota=7.0` | `30` (bônus incluído) |
| 8 | Entrega com status RASCUNHO | `RASCUNHO`, `nota=10` | `0` (ignorada) |
| 9 | Entrega com status NAO_INICIADA | `NAO_INICIADA` | `0` (ignorada) |
| 10 | 5 revisões de flashcard, sem entregas | `totalRevisoes = [2, 3]` (soma=5) | `5` |
| 11 | Mix: 2 entregas + flashcards | `ENTREGUE no prazo nota=8`, `CORRIGIDA atrasada nota=null`, `5 revisões` | `30 + 3 + 5 = 38` |

---

## Método: `verificarConquistas(UUID alunoId)`

### Regras de conquistas

| Conquista | Critério | Tipo |
|-----------|----------|------|
| DEDICADO | 10+ entregas no prazo (não atrasadas, status ENTREGUE ou CORRIGIDA) | `"DEDICADO"` |
| EXCELENCIA | 5+ entregas com nota final >= 9.0 | `"EXCELENCIA"` |
| FLASHMASTER | 50+ revisões totais de flashcards (soma de `totalRevisoes`) | `"FLASHMASTER"` |

### Cenários — DEDICADO

| # | Cenário | Dados | Resultado |
|---|---------|-------|-----------|
| 1 | 10 entregas no prazo, conquista ainda não existe | 10 ENTREGUE + !atrasada, `existsByAlunoIdAndTipo` = false | `salvarConquista` chamado com `"DEDICADO"` |
| 2 | 10 entregas no prazo, conquista já existe | mesmo + `existsByAlunoIdAndTipo` = true | `salvarConquista` **não** chamado |
| 3 | Menos de 10 entregas | 9 entregas | `salvarConquista` **não** chamado |
| 4 | 10 entregas mas algumas atrasadas | 7 no prazo + 3 atrasadas | `salvarConquista` **não** chamado |
| 5 | Entregas em RASCUNHO não contam | 10 RASCUNHO + !atrasada | `salvarConquista` **não** chamado |

### Cenários — EXCELENCIA

| # | Cenário | Dados | Resultado |
|---|---------|-------|-----------|
| 6 | 5 notas >= 9.0, conquista não existe | notas=[9.0, 9.5, 10.0, 9.1, 9.8], `existsByAlunoIdAndTipo` = false | `salvarConquista` com `"EXCELENCIA"` |
| 7 | 5 notas >= 9.0, conquista já existe | mesmo + `existsByAlunoIdAndTipo` = true | não chama `salvarConquista` |
| 8 | Nota exata 9.0 conta | `nota=9.0` | conta para o critério |
| 9 | Nota 8.9 não conta | `nota=8.9` | não conta |
| 10 | Entregas sem nota não contam | `nota=null` | ignoradas |

### Cenários — FLASHMASTER

| # | Cenário | Dados | Resultado |
|---|---------|-------|-----------|
| 11 | Soma >= 50, conquista não existe | estados com `totalRevisoes=[20, 30]`, soma=50 | `salvarConquista` com `"FLASHMASTER"` |
| 12 | Soma < 50 | `totalRevisoes=[10, 20]`, soma=30 | não chama |
| 13 | Conquista já existe | soma=50, `existsByAlunoIdAndTipo` = true | não chama |
| 14 | Nenhuma revisão | lista vazia | não chama |

---

## Método: `getConquistas(UUID alunoId)`

| # | Cenário | Dados | Saída Esperada |
|---|---------|-------|----------------|
| 1 | Aluno com conquistas | 2 conquistas no repositório | lista com 2 `ConquistaDto` na ordem correta |
| 2 | Aluno sem conquistas | lista vazia | lista vazia |
| 3 | Mapeamento correto | conquista com `tipo="DEDICADO"`, `descricao="..."`, `obtidaEm=...` | DTO com os mesmos valores |

---

## Regras de Negócio Críticas

- Conquistas são **idempotentes** — nunca duplicar se já existir.
- A pontuação considera **todas** as entregas ENTREGUE e CORRIGIDA — independente da nota.
- O bônus de nota é calculado sobre `notaFinal`, não `notaAutomatica`.
- Flashcards somam diretamente pela quantidade total de revisões do estado SM-2.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class GamificacaoServiceTest {

    @Mock EntregaRepository entregaRepository;
    @Mock FlashcardEstadoSm2Repository estadoSm2Repository;
    @Mock ConquistaRepository conquistaRepository;

    @InjectMocks GamificacaoService gamificacaoService;

    private Entrega entregaFake(StatusEntregaEnum status, boolean atrasada, BigDecimal nota) {
        var e = new Entrega();
        e.setStatus(status);
        e.setEntregaAtrasada(atrasada);
        e.setNotaFinal(nota);
        return e;
    }
}
```
