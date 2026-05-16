# Spec de Testes Unitários — AvaliacaoService

**Classe:** `br.com.inovadados.teacherplatform.service.AvaliacaoService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/AvaliacaoService.java`

---

## Visão Geral

`AvaliacaoService` gerencia o ciclo de vida de avaliações: criação de rascunho, atualização, publicação e preview. O núcleo testável é a lógica de transição de status, o agendamento automático e a serialização de alternativas.

**Dependências para mock:** `AvaliacaoRepository`, `QuestaoRepository`, `TurmaRepository`, `TurmaAvaliacaoRepository`, `UsuarioRepository`, `ApplicationEventPublisher`, `ObjectMapper`.

---

## Método: `salvarRascunho(SalvarRascunhoRequest, UUID professorId)`

| # | Cenário | Condições | Saída Esperada |
|---|---------|-----------|----------------|
| 1 | Turma não encontrada | `turmaRepository.findById` retorna empty | lança `TurmaNaoEncontradaException` |
| 2 | Professor não encontrado | `usuarioRepository.findById` retorna empty | lança `UnauthorizedException` |
| 3 | Avaliação criada com status RASCUNHO | qualquer entrada válida | `avaliacao.status == RASCUNHO` |
| 4 | Defaults aplicados | sem configurações de embaralhamento | `embaralharQuestoes=false`, `embaralharAlternativas=false`, `gabaritoLiberacao=APOS_ENCERRAMENTO` |
| 5 | Questões salvas quando fornecidas | `req.questoes != null` | `questaoRepository.save` chamado para cada questão |
| 6 | Questões ignoradas quando null | `req.questoes = null` | `questaoRepository.save` não chamado |
| 7 | Ordem das questões sequencial | 3 questões no request | questões salvas com ordem 1, 2, 3 |
| 8 | Ponto padrão quando null | `questaoDto.pontos = null` | questão salva com `pontos = BigDecimal.ONE` |

---

## Método: `atualizarRascunho(Long id, SalvarRascunhoRequest, UUID professorId)`

| # | Cenário | Condições | Saída Esperada |
|---|---------|-----------|----------------|
| 1 | Avaliação não encontrada | `findById` retorna empty | lança `AvaliacaoNaoEncontradaException` |
| 2 | Status diferente de RASCUNHO | `status = PUBLICADA` | lança `OperacaoNaoPermitidaException` |
| 3 | Status AGENDADA bloqueia edição | `status = AGENDADA` | lança `OperacaoNaoPermitidaException` |
| 4 | Questões antigas removidas antes de salvar novas | `req.questoes != null` | `questaoRepository.deleteAll` chamado antes de salvar novas |
| 5 | Questões não alteradas quando null | `req.questoes = null` | `questaoRepository.deleteAll` não chamado |

---

## Método: `publicar(Long id, PublicarAvaliacaoRequest)`

### Transição de status (RASCUNHO → PUBLICADA ou AGENDADA)

| # | Cenário | `disponivelEm` | Status Esperado |
|---|---------|---------------|-----------------|
| 1 | Data no futuro | 1h à frente | `AGENDADA` |
| 2 | Data no passado | 1h atrás | `PUBLICADA` |
| 3 | Data = agora (borderline) | exatamente agora | `PUBLICADA` (não é "depois" de agora) |

### Validações

| # | Cenário | Condições | Exceção |
|---|---------|-----------|---------|
| 4 | Status não é RASCUNHO | `status = PUBLICADA` | `OperacaoNaoPermitidaException` |
| 5 | Status AGENDADA | `status = AGENDADA` | `OperacaoNaoPermitidaException` |

### Comportamentos

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 6 | TurmaAvaliacao criada para cada turma | `turmasIds = [1L, 2L, 3L]` | `turmaAvaliacaoRepository.save` chamado 3 vezes |
| 7 | Evento publicado após salvar | publicação ok | `eventPublisher.publishEvent` chamado com `AvaliacaoPublicadaEvent` |
| 8 | PesoNota definido se fornecido | `req.pesoNota = 2.5` | `avaliacao.pesoNota = 2.5` |
| 9 | PesoNota ignorado se null | `req.pesoNota = null` | valor anterior preservado |

---

## Método: `preview(Long id, UUID seed)`

| # | Cenário | Condições | Saída Esperada |
|---|---------|-----------|----------------|
| 1 | Avaliação não encontrada | `findById` retorna empty | lança `AvaliacaoNaoEncontradaException` |
| 2 | Sem embaralhamento | `embaralharQuestoes = false` | questões na ordem original |
| 3 | Com embaralhamento determinístico | `embaralharQuestoes = true`, mesmo `seed` | mesma ordem em duas chamadas |
| 4 | Seeds diferentes geram ordens diferentes | `seed1 ≠ seed2` | ordens possivelmente diferentes |
| 5 | Gabarito não incluído no preview | questão com `gabaritoIndice = 2` | `QuestaoAvaliacaoDto.gabaritoIndice = null` |

---

## Método: `salvarQuestoes` — Serialização de Alternativas (private)

| # | Cenário | Entrada | Comportamento |
|---|---------|---------|---------------|
| 1 | Alternativas serializadas como JSON | `["A", "B", "C"]` | `questao.alternativas = "[\"A\",\"B\",\"C\"]"` |
| 2 | Alternativas null não serializada | `dto.alternativas = null` | `questao.alternativas = null` |
| 3 | Lista vazia não serializada | `dto.alternativas = []` | `questao.alternativas = null` |

---

## Regras de Negócio Críticas

- Só avaliações em **RASCUNHO** podem ser editadas ou publicadas.
- A transição para AGENDADA/PUBLICADA é determinada pela comparação de `disponivelEm` com o momento atual.
- O evento `AvaliacaoPublicadaEvent` deve ser disparado **sempre** que publicar, independente do status resultante (AGENDADA ou PUBLICADA).
- Questões na atualização são **sempre recriadas** (delete + insert), nunca atualizadas in-place.
- Ponto padrão de uma questão é **1.0** quando não fornecido.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock TurmaAvaliacaoRepository turmaAvaliacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Spy  ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks AvaliacaoService avaliacaoService;
}
```
