# Testes Unitários — AtividadeService: Entrega e Rascunho

> **Escopo:** Backend — `AtividadeService.entregar` e `salvarRascunho`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `test-atividade-service_calculo-nota-automatica.md` (mesma classe)

---

## Contexto

`entregar` e `salvarRascunho` controlam o ciclo de vida da participação do aluno em uma atividade. O prazo de entrega, a idempotência (não entregar duas vezes) e a transição correta de status são regras de negócio que devem ser garantidas nos testes.

---

## O que deve ser implementado

Adicionar à `AtividadeServiceTest` os cenários de `entregar` e `salvarRascunho`, incluindo validações de prazo, bloqueio de re-entrega e lógica de status.

---

## Critérios de Aceite

**`entregar`:**
- [ ] Avaliação não encontrada → `AvaliacaoNaoEncontradaException`
- [ ] Entrega dentro do prazo → `entregaAtrasada = false`
- [ ] Entrega após prazo com `permiteEntregaAtrasada = true` → `entregaAtrasada = true`, aceita
- [ ] Entrega após prazo com `permiteEntregaAtrasada = false` → `OperacaoNaoPermitidaException`
- [ ] Status ENTREGUE → `OperacaoNaoPermitidaException` (já entregue)
- [ ] Status CORRIGIDA → `OperacaoNaoPermitidaException` (já entregue)
- [ ] `iniciadoEm` preenchido se era null antes da entrega
- [ ] `iniciadoEm` preservado se já estava preenchido
- [ ] Nota automática calculada e salva em `notaAutomatica` e `notaFinal`
- [ ] Status muda para ENTREGUE
- [ ] `entregueEm` preenchido
- [ ] Gabarito retornado na resposta se `gabaritoLiberacao = IMEDIATA`
- [ ] Gabarito vazio na resposta se avaliação ainda aberta

**`salvarRascunho`:**
- [ ] Status ENTREGUE → `OperacaoNaoPermitidaException`
- [ ] Status CORRIGIDA → `OperacaoNaoPermitidaException`
- [ ] Primeira vez: cria entrega com status `NAO_INICIADA` → muda para `RASCUNHO`
- [ ] Rascunho existente: status não alterado
- [ ] `iniciadoEm` preenchido na primeira vez (status era NAO_INICIADA)
- [ ] `entregaRepository.save` chamado

**`getResultado`:**
- [ ] Entrega não encontrada → `OperacaoNaoPermitidaException`
- [ ] Aluno diferente → `OperacaoNaoPermitidaException` ("Acesso negado")
- [ ] Gabarito populado se disponível
- [ ] `analise` contém acertos por tópico

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AtividadeService.md`
- **Seções:** `entregar`, `salvarRascunho`, `getResultado`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AtividadeServiceTest.java`

**Para simular prazo encerrado:**
```java
var avaliacao = new Avaliacao();
avaliacao.setEncerraEm(OffsetDateTime.now().minusHours(1)); // já encerrou
avaliacao.setPermiteEntregaAtrasada(false);
```

---

## Notas e Edge Cases

- `entregar` usa `OffsetDateTime.now()` internamente — se precisar controlar o "agora" nos testes, usar `spy` ou extrair o clock para injeção
- `calcularNotaAutomatica` é chamado internamente — o resultado depende das questões mockadas

---

## Definition of Done

- [ ] Cenários de `entregar`, `salvarRascunho` e `getResultado` adicionados à `AtividadeServiceTest`
- [ ] Mínimo 20 testes adicionais
- [ ] Suite completa passa com `./mvnw test -Dtest=AtividadeServiceTest`
