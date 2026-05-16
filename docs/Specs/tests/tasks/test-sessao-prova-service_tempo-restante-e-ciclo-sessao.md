# Testes Unitários — SessaoProvaService: Tempo Restante e Ciclo de Sessão

> **Escopo:** Backend — `SessaoProvaService`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`SessaoProvaService` controla a sessão de prova do aluno com tempo limitado. `calcularTempoRestanteSegundos` é o coração do mecanismo de expiração automática — retornar um valor negativo ou incorreto pode encerrar sessões prematuramente ou não encerrá-las nunca. O método é package-private, permitindo teste direto.

---

## O que deve ser implementado

Criar `SessaoProvaServiceTest` cobrindo: `calcularTempoRestanteSegundos` (direto), e os fluxos de `iniciar`, `autosave`, `entregar` e `encerrarPorExpiracao` via mocks.

---

## Critérios de Aceite

**`calcularTempoRestanteSegundos`:**
- [ ] `duracaoMinutos = null` → `Long.MAX_VALUE`
- [ ] Sessão iniciada há 10 min, duração 60 min → ≈ 3000s (tolerância ±5s)
- [ ] Sessão iniciada há 90 min, duração 60 min → `0` (nunca negativo)
- [ ] Sessão expirada exatamente agora → `0`

**`iniciar`:**
- [ ] Prova não encontrada → `AvaliacaoNaoEncontradaException`
- [ ] Prova não PUBLICADA → `OperacaoNaoPermitidaException`
- [ ] `disponivelEm` no futuro → `OperacaoNaoPermitidaException`
- [ ] `encerraEm` no passado → `OperacaoNaoPermitidaException`
- [ ] Sessão já encerrada (`encerradaEm != null`) → `OperacaoNaoPermitidaException`
- [ ] Primeira vez: cria sessão com `iniciadaEm` preenchido
- [ ] Sessão existente: reutiliza sem criar nova
- [ ] `duracaoMinutos = null` → `SessaoProvaResponse.duracaoMinutos = 0`

**`autosave`:**
- [ ] Sessão não encontrada → `OperacaoNaoPermitidaException`
- [ ] Sessão de outro aluno → `OperacaoNaoPermitidaException`
- [ ] Sessão encerrada → `OperacaoNaoPermitidaException`
- [ ] Status NAO_INICIADA → muda para RASCUNHO, `iniciadoEm = sessao.iniciadaEm`
- [ ] Status RASCUNHO → não muda status
- [ ] Respostas `null` → `save` de resposta não chamado

**`entregar`:**
- [ ] Sessão não encontrada → `OperacaoNaoPermitidaException`
- [ ] Sessão de outro aluno → `OperacaoNaoPermitidaException`
- [ ] Sessão encerrada → `OperacaoNaoPermitidaException`
- [ ] Delega para `atividadeService.entregar` com respostas parciais
- [ ] `sessao.encerradaEm` preenchido após entrega
- [ ] `sessao.entregueManualmente = true`

**`encerrarPorExpiracao`:**
- [ ] Entrega bem-sucedida → `sessao.entregueManualmente = false`, `encerradaEm` preenchido
- [ ] `OperacaoNaoPermitidaException` → warn logado, sessão encerrada mesmo assim
- [ ] Exceção genérica → erro logado, sessão encerrada mesmo assim

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/SessaoProvaService.md`
- **Seções:** Todos os métodos

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/SessaoProvaServiceTest.java`

**Acesso direto ao método package-private:**
```java
long tempo = sessaoProvaService.calcularTempoRestanteSegundos(sessao);
```

**Helper:**
```java
private SessaoProva sessaoFake(UUID alunoId, Integer duracaoMin, OffsetDateTime encerrada) {
    var s = new SessaoProva();
    var aluno = new Usuario(); aluno.setId(alunoId);
    var av = new Avaliacao(); av.setDuracaoMinutos(duracaoMin);
    s.setAluno(aluno); s.setAvaliacao(av);
    s.setIniciadaEm(OffsetDateTime.now().minusMinutes(10));
    s.setEncerradaEm(encerrada);
    return s;
}
```

---

## Notas e Edge Cases

- Para tempo restante: o resultado pode variar ±1s dependendo de quando o teste roda — usar tolerância com `assertThat(tempo).isCloseTo(3000L, within(10L))`
- `encerrarPorExpiracao` não deve propagar exceções — verificar que o teste não falha mesmo quando `atividadeService.entregar` lança

---

## Definition of Done

- [ ] Classe `SessaoProvaServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 23 testes)
- [ ] Testes passam com `./mvnw test -Dtest=SessaoProvaServiceTest`
