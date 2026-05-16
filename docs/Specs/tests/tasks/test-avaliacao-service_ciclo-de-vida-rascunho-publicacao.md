# Testes Unitários — AvaliacaoService: Ciclo de Vida (Rascunho e Publicação)

> **Escopo:** Backend — `AvaliacaoService`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`AvaliacaoService` gerencia o ciclo de vida das avaliações criadas pelos professores. A transição entre os estados RASCUNHO → AGENDADA/PUBLICADA é a lógica central — qualquer erro pode publicar avaliações antes do prazo ou bloquear a publicação indevidamente. O embaralhamento determinístico (seed-based) no preview também precisa de cobertura.

---

## O que deve ser implementado

Criar `AvaliacaoServiceTest` cobrindo: `salvarRascunho`, `atualizarRascunho`, `publicar` e `preview`. Mockar todos os repositórios e `ApplicationEventPublisher`.

**Foco principal:**
- Defaults aplicados ao criar rascunho
- Bloqueio de edição para status ≠ RASCUNHO
- Transição para AGENDADA vs PUBLICADA com base na data
- Evento `AvaliacaoPublicadaEvent` disparado na publicação
- Preview com e sem embaralhamento

---

## Critérios de Aceite

**`salvarRascunho`:**
- [ ] Turma não encontrada → `TurmaNaoEncontradaException`
- [ ] Professor não encontrado → `UnauthorizedException`
- [ ] Avaliação criada com `status = RASCUNHO`
- [ ] Defaults: `embaralharQuestoes=false`, `embaralharAlternativas=false`, `gabaritoLiberacao=APOS_ENCERRAMENTO`, `geradoPorIa=false`
- [ ] Questões salvas quando `req.questoes != null`
- [ ] Questões ignoradas quando `req.questoes = null`
- [ ] Ordem das questões: sequencial 1, 2, 3...
- [ ] Questão sem pontos recebe `pontos = 1.0` (BigDecimal.ONE)

**`atualizarRascunho`:**
- [ ] Avaliação não encontrada → `AvaliacaoNaoEncontradaException`
- [ ] Status ≠ RASCUNHO → `OperacaoNaoPermitidaException`
- [ ] Com questões: `deleteAll` chamado antes de salvar novas
- [ ] Sem questões (`null`): `deleteAll` não chamado

**`publicar`:**
- [ ] Status ≠ RASCUNHO → `OperacaoNaoPermitidaException`
- [ ] `disponivelEm` no futuro → `status = AGENDADA`
- [ ] `disponivelEm` no passado → `status = PUBLICADA`
- [ ] `TurmaAvaliacao` criada para cada turma em `req.turmasIds`
- [ ] `eventPublisher.publishEvent` chamado com `AvaliacaoPublicadaEvent`
- [ ] `pesoNota` atualizado quando fornecido; preservado quando `null`

**`preview`:**
- [ ] Avaliação não encontrada → `AvaliacaoNaoEncontradaException`
- [ ] Sem embaralhamento: questões na ordem original
- [ ] Com embaralhamento + mesmo seed: mesma ordem em duas chamadas
- [ ] Gabarito não exposto: `gabaritoIndice` e `gabaritoDissertativo` = null nas questões

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AvaliacaoService.md`
- **Seções:** Todos os métodos documentados

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AvaliacaoServiceTest.java`

**Para testar agendamento:**
```java
// AGENDADA
var req = new PublicarAvaliacaoRequest(OffsetDateTime.now().plusHours(1), ...);
// PUBLICADA
var req = new PublicarAvaliacaoRequest(OffsetDateTime.now().minusHours(1), ...);
```

---

## Notas e Edge Cases

- O embaralhamento usa `new Random(seed)` — determinístico, mesmo seed = mesma ordem
- A `AvaliacaoPublicadaEvent` deve ser disparada mesmo quando o status resultante é AGENDADA
- `deleteAll` é chamado com o resultado de `findByAvaliacaoIdOrderByOrdem` — mockar esse método

---

## Definition of Done

- [ ] Classe `AvaliacaoServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 22 testes)
- [ ] Testes passam com `./mvnw test -Dtest=AvaliacaoServiceTest`
