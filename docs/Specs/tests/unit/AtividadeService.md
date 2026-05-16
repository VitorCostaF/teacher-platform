# Spec de Testes Unitários — AtividadeService

**Classe:** `br.com.inovadados.teacherplatform.service.AtividadeService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/AtividadeService.java`

---

## Visão Geral

`AtividadeService` gerencia o ciclo de vida de entregas de atividades por alunos. O núcleo testável são: cálculo automático de notas, resolução de status de entrega, verificação de gabarito e validação de prazo.

**Dependências para mock:** `AvaliacaoRepository`, `EntregaRepository`, `QuestaoRepository`, `RespostaRepository`, `UsuarioRepository`, `ObjectMapper`.

---

## Método: `calcularNotaAutomatica` (package-private)

### Fórmula: `(pontos obtidos / total possível) * 10`, arredondado `HALF_UP` com 2 casas.

**Tipos de questão ignorados:** `DISSERTATIVA`, `UPLOAD_ARQUIVO` — não entram no cálculo.  
**Tipos computados:** `MULTIPLA_ESCOLHA`, `VERDADEIRO_FALSO`.

| # | Cenário | Questões | Respostas | Nota Esperada |
|---|---------|----------|-----------|---------------|
| 1 | Todas corretas | 2 questões, 5pts cada | ambas corretas | `10.00` |
| 2 | Nenhuma correta | 2 questões, 5pts cada | ambas erradas | `0.00` |
| 3 | Metade correta | 2 questões, 5pts cada | 1 certa, 1 errada | `5.00` |
| 4 | Pesos diferentes | Q1=3pts, Q2=7pts; só Q1 certa | resposta Q1 correta | `3.00` |
| 5 | Questão dissertativa ignorada | 1 DISSERTATIVA 10pts + 1 MC 10pts correta | MC certa | `10.00` |
| 6 | Apenas dissertativas | 2 DISSERTATIVA | qualquer | `0.00` (total=0, retorna zero) |
| 7 | Questão sem gabarito | `gabaritoIndice = null` | qualquer | questão não pontuada |
| 8 | Resposta não enviada | questão com gabarito, sem resposta | `respostas.get(questaoId) = null` | não pontua |
| 9 | Arredondamento | Q1=3pts, Q2=3pts, Q3=4pts; 2 certas (7pts de 10) | Q1 e Q2 corretas | `7.00` |
| 10 | Arredondamento HALF_UP | 1 de 3 questões, todas 1pt | 1 certa | `3.33` (1/3 * 10) |

---

## Método: `resolverStatus(Entrega entrega)` (private — testado via `getAtividade`)

| # | Entrega | Status Esperado |
|---|---------|-----------------|
| 1 | `entrega = null` | `"NAO_INICIADO"` |
| 2 | `entrega.status = NAO_INICIADA` | `"NAO_INICIADO"` |
| 3 | `entrega.status = RASCUNHO` | `"EM_ANDAMENTO"` |
| 4 | `entrega.status = ENTREGUE` | `"ENTREGUE"` |
| 5 | `entrega.status = CORRIGIDA` | `"ENTREGUE"` |

---

## Método: `isGabaritoDisponivel(Avaliacao avaliacao)` (private — testado via `entregar` / `getAtividade`)

| # | Cenário | `gabaritoLiberacao` | `encerraEm` | Disponível? |
|---|---------|---------------------|-------------|-------------|
| 1 | Liberação imediata | `IMEDIATA` | qualquer | `true` |
| 2 | Liberação após encerramento — já encerrou | `APOS_ENCERRAMENTO` | 1h atrás | `true` |
| 3 | Liberação após encerramento — ainda aberta | `APOS_ENCERRAMENTO` | 1h à frente | `false` |
| 4 | Após encerramento — sem data | `APOS_ENCERRAMENTO` | `null` | `false` |
| 5 | Nunca liberar (ou outro valor) | qualquer outro | qualquer | `false` |

---

## Método: `entregar(Long avaliacaoId, SalvarRascunhoAtividadeRequest, UUID alunoId)`

| # | Cenário | Dados | Saída Esperada |
|---|---------|-------|----------------|
| 1 | Avaliação não encontrada | `avaliacaoRepository.findById` retorna empty | lança `AvaliacaoNaoEncontradaException` |
| 2 | Entrega no prazo | `encerraEm` = amanhã | `entregaAtrasada = false` |
| 3 | Entrega atrasada, permite atraso | `encerraEm` = ontem, `permiteEntregaAtrasada = true` | `entregaAtrasada = true`, entrega aceita |
| 4 | Entrega atrasada, não permite | `encerraEm` = ontem, `permiteEntregaAtrasada = false` | lança `OperacaoNaoPermitidaException` |
| 5 | Já entregue | `entrega.status = ENTREGUE` | lança `OperacaoNaoPermitidaException` |
| 6 | Já corrigida | `entrega.status = CORRIGIDA` | lança `OperacaoNaoPermitidaException` |
| 7 | `iniciadoEm` preenchido se null | entrega nova sem `iniciadoEm` | `iniciadoEm` = agora |
| 8 | Nota calculada e salva | questões com gabarito | `notaAutomatica` e `notaFinal` preenchidos |
| 9 | Status muda para ENTREGUE | entrega em RASCUNHO | `status = ENTREGUE` após entrega |
| 10 | Gabarito retornado se disponível | `gabaritoLiberacao = IMEDIATA` | `gabarito` na resposta com questões |
| 11 | Gabarito vazio se indisponível | `gabaritoLiberacao = APOS_ENCERRAMENTO`, ainda aberta | `gabarito = []` |

---

## Método: `salvarRascunho(Long avaliacaoId, SalvarRascunhoAtividadeRequest, UUID alunoId)`

| # | Cenário | Dados | Saída Esperada |
|---|---------|-------|----------------|
| 1 | Primeira vez — cria entrega | `findByAvaliacaoIdAndAlunoId` retorna empty | entrega criada com `NAO_INICIADA` → `RASCUNHO` |
| 2 | Rascunho existente — só atualiza | entrega com `RASCUNHO` | não muda status |
| 3 | Entrega já enviada | `status = ENTREGUE` | lança `OperacaoNaoPermitidaException` |
| 4 | Entrega corrigida | `status = CORRIGIDA` | lança `OperacaoNaoPermitidaException` |
| 5 | `iniciadoEm` preenchido ao iniciar | status `NAO_INICIADA` | `iniciadoEm` = agora |

---

## Método: `getResultado(Long entregaId, UUID alunoId)`

| # | Cenário | Dados | Saída Esperada |
|---|---------|-------|----------------|
| 1 | Entrega não encontrada | `findById` retorna empty | lança `OperacaoNaoPermitidaException` |
| 2 | Acesso negado | entrega de outro aluno | lança `OperacaoNaoPermitidaException` |
| 3 | Gabarito disponível | `IMEDIATA` | `gabarito` populado |
| 4 | Gabarito indisponível | `APOS_ENCERRAMENTO`, aberta | `gabarito = []` |
| 5 | Análise por tópico | questões com tópicos, respostas corretas/erradas | `AnaliseTopicoDto` com acertos por tópico |

---

## Regras de Negócio Críticas

- Questões `DISSERTATIVA` e `UPLOAD_ARQUIVO` **nunca** entram no cálculo automático de nota.
- Se todas as questões são dissertativas, a nota retorna `0.00` (total=0, sem divisão por zero).
- A nota usa `(obtido * 10) / total` na escala de 0 a 10.
- Um aluno só pode entregar uma vez — status `ENTREGUE` ou `CORRIGIDA` bloqueia nova entrega.
- `entregaAtrasada` é definida no momento da entrega, não no rascunho.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {

    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Spy  ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks AtividadeService atividadeService;

    private Questao questaoMC(Long id, int gabaritoIndice, BigDecimal pontos) {
        var q = new Questao();
        q.setId(id);
        q.setTipo(TipoQuestaoEnum.MULTIPLA_ESCOLHA);
        q.setGabaritoIndice(gabaritoIndice);
        q.setPontos(pontos);
        return q;
    }
}
```
