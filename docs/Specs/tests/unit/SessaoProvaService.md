# Spec de Testes Unitários — SessaoProvaService

**Classe:** `br.com.inovadados.teacherplatform.service.SessaoProvaService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/SessaoProvaService.java`

---

## Visão Geral

`SessaoProvaService` controla sessões de prova com tempo limitado: abertura, autosave e entrega. O ponto mais crítico é `calcularTempoRestanteSegundos`, que determina quando a sessão expira automaticamente.

**Dependências para mock:** `SessaoProvaRepository`, `AvaliacaoRepository`, `EntregaRepository`, `QuestaoRepository`, `RespostaRepository`, `UsuarioRepository`, `AtividadeService`.

---

## Método: `calcularTempoRestanteSegundos(SessaoProva sessao)` (package-private)

**Fórmula:** `max(0, expiracao - agora)` onde `expiracao = iniciadaEm + duracaoMinutos`.

| # | Cenário | Dados | Resultado Esperado |
|---|---------|-------|-------------------|
| 1 | Duração null | `duracaoMinutos = null` | `Long.MAX_VALUE` (sem limite) |
| 2 | Tempo ainda disponível | início=agora-10min, duração=60min | ≈ 3000 segundos (50 min) |
| 3 | Tempo expirado | início=agora-90min, duração=60min | `0` (nunca negativo) |
| 4 | Expirou exatamente agora | `expiracao == agora` | `0` |
| 5 | Iniciado há 1 min, duração 1 min | restam 0s | `0` |

---

## Método: `iniciar(Long provaId, UUID alunoId)`

### Validações de acesso

| # | Cenário | Condições | Exceção |
|---|---------|-----------|---------|
| 1 | Prova não encontrada | `avaliacaoRepository.findById` retorna empty | `AvaliacaoNaoEncontradaException` |
| 2 | Prova não PUBLICADA | `status = RASCUNHO` | `OperacaoNaoPermitidaException` |
| 3 | Prova ainda não disponível | `disponivelEm` no futuro | `OperacaoNaoPermitidaException` |
| 4 | Prova já encerrada | `encerraEm` no passado | `OperacaoNaoPermitidaException` |
| 5 | Sessão já encerrada | sessão existente com `encerradaEm != null` | `OperacaoNaoPermitidaException` |

### Comportamentos felizes

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 6 | Primeira vez — cria sessão | `sessaoProvaRepository.findBy...` retorna empty | `criarSessao` chamado; `sessao.iniciadaEm` preenchido |
| 7 | Sessão já existe — reutiliza | sessão existente sem `encerradaEm` | não cria nova sessão |
| 8 | Duração null retorna 0 | `duracaoMinutos = null` | `SessaoProvaResponse.duracaoMinutos = 0` |
| 9 | Respostas parciais carregadas | entrega em RASCUNHO com respostas | `respostasParciais` preenchido na resposta |
| 10 | Sem respostas se sem entrega | nenhuma entrega | `respostasParciais = Map.of()` |

---

## Método: `autosave(Long sessaoId, AutosaveProvaRequest, UUID alunoId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Sessão não encontrada | `sessaoProvaRepository.findById` retorna empty | lança `OperacaoNaoPermitidaException` |
| 2 | Sessão de outro aluno | `sessao.aluno.id ≠ alunoId` | lança `OperacaoNaoPermitidaException` |
| 3 | Sessão já encerrada | `sessao.encerradaEm != null` | lança `OperacaoNaoPermitidaException` |
| 4 | Entrega NAO_INICIADA → RASCUNHO | status inicial = NAO_INICIADA | `entrega.status = RASCUNHO`, `iniciadoEm` = `sessao.iniciadaEm` |
| 5 | Entrega já em RASCUNHO — não muda status | `entrega.status = RASCUNHO` | status não alterado |
| 6 | Respostas salvas | `req.respostas != null` | `respostaRepository.save` chamado para cada questão respondida |
| 7 | Respostas null ignoradas | `req.respostas = null` | `respostaRepository.save` não chamado |
| 8 | Evento de visibilidade logado | `req.eventoVisibilidade != null` | log registrado (sem alteração de estado) |

---

## Método: `entregar(Long provaId, Long sessaoId, UUID alunoId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Sessão não encontrada | `findById` retorna empty | lança `OperacaoNaoPermitidaException` |
| 2 | Sessão de outro aluno | `sessao.aluno.id ≠ alunoId` | lança `OperacaoNaoPermitidaException` |
| 3 | Sessão já encerrada | `encerradaEm != null` | lança `OperacaoNaoPermitidaException` |
| 4 | Entrega delegada ao AtividadeService | sessão válida | `atividadeService.entregar` chamado com respostas parciais |
| 5 | Sessão marcada como encerrada manualmente | entrega ok | `sessao.encerradaEm` != null, `entregueManualmente = true` |
| 6 | Sessão salva após entrega | entrega ok | `sessaoProvaRepository.save` chamado |

---

## Método: `encerrarPorExpiracao(SessaoProva sessao)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Entrega bem-sucedida | `atividadeService.entregar` ok | `sessao.encerradaEm` preenchido, `entregueManualmente = false` |
| 2 | Já entregue (OperacaoNaoPermitida) | serviço lança `OperacaoNaoPermitidaException` | warn logado; sessão marcada como encerrada mesmo assim |
| 3 | Erro inesperado | serviço lança `RuntimeException` | erro logado; sessão ainda marcada como encerrada |
| 4 | `entregueManualmente = false` sempre | expiração | `sessao.entregueManualmente = false` |

---

## Regras de Negócio Críticas

- `calcularTempoRestanteSegundos` nunca retorna valor negativo — mínimo é `0`.
- Sem `duracaoMinutos`, a sessão não tem limite de tempo (`Long.MAX_VALUE`).
- `autosave` pode ser chamado múltiplas vezes — é idempotente para o status da entrega.
- `encerrarPorExpiracao` não propaga exceções — erros são logados, sessão encerrada de qualquer forma.
- A sessão deve existir apenas uma por `(provaId, alunoId)`.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class SessaoProvaServiceTest {

    @Mock SessaoProvaRepository sessaoProvaRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AtividadeService atividadeService;

    @InjectMocks SessaoProvaService sessaoProvaService;

    private SessaoProva sessaoFake(UUID alunoId, int duracaoMin, OffsetDateTime encerrada) {
        var s = new SessaoProva();
        var aluno = new Usuario(); aluno.setId(alunoId);
        var av = new Avaliacao(); av.setDuracaoMinutos(duracaoMin);
        s.setAluno(aluno);
        s.setAvaliacao(av);
        s.setIniciadaEm(OffsetDateTime.now().minusMinutes(10));
        s.setEncerradaEm(encerrada);
        return s;
    }
}
```
