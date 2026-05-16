# Testes Unitários — AtividadeService: Cálculo de Nota Automática

> **Escopo:** Backend — `AtividadeService.calcularNotaAutomatica`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`calcularNotaAutomatica` computa a nota do aluno com base nas respostas às questões objetivas. É o único cálculo financeiramente relevante do sistema — uma nota errada afeta a situação acadêmica do aluno. O método é package-private, permitindo teste direto sem mockar o fluxo completo de entrega.

---

## O que deve ser implementado

Criar `AtividadeServiceTest` e testar `calcularNotaAutomatica` diretamente (acesso package-private). Completar com testes dos métodos auxiliares `resolverStatus` e `isGabaritoDisponivel`, verificados indiretamente via `getAtividade` e `entregar`.

**Fórmula:** `(pontos obtidos / total possível) * 10`, arredondado `HALF_UP` com 2 casas.  
**Questões ignoradas:** `DISSERTATIVA` e `UPLOAD_ARQUIVO`.

---

## Critérios de Aceite

**`calcularNotaAutomatica`:**
- [ ] Todas as questões corretas → `10.00`
- [ ] Nenhuma questão correta → `0.00`
- [ ] Metade correta, pesos iguais → `5.00`
- [ ] Questão DISSERTATIVA ignorada no denominador e numerador
- [ ] Apenas dissertativas → `0.00` (proteção contra divisão por zero)
- [ ] Questão sem `gabaritoIndice` → não pontua (não lança exceção)
- [ ] Resposta ausente → não pontua
- [ ] Arredondamento HALF_UP: 1 de 3 questões = `3.33`
- [ ] Pesos diferentes: Q1=3pts correta, Q2=7pts errada → `3.00` (3/10 * 10)

**`resolverStatus` (via `getAtividade`):**
- [ ] `entrega = null` → `"NAO_INICIADO"`
- [ ] `status = NAO_INICIADA` → `"NAO_INICIADO"`
- [ ] `status = RASCUNHO` → `"EM_ANDAMENTO"`
- [ ] `status = ENTREGUE` → `"ENTREGUE"`
- [ ] `status = CORRIGIDA` → `"ENTREGUE"`

**`isGabaritoDisponivel`:**
- [ ] `IMEDIATA` → sempre `true`
- [ ] `APOS_ENCERRAMENTO` + `encerraEm` no passado → `true`
- [ ] `APOS_ENCERRAMENTO` + `encerraEm` no futuro → `false`
- [ ] `APOS_ENCERRAMENTO` + `encerraEm = null` → `false`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AtividadeService.md`
- **Seções:** `calcularNotaAutomatica`, `resolverStatus`, `isGabaritoDisponivel`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AtividadeServiceTest.java`

**Acesso direto ao método package-private** (mesmo pacote):
```java
// O teste está em br.com.inovadados.teacherplatform.service
BigDecimal nota = atividadeService.calcularNotaAutomatica(entregaId, questoes, respostas);
```

**Helper:**
```java
private Questao questaoMC(Long id, int gabaritoIndice, String pontos) {
    var q = new Questao();
    q.setId(id);
    q.setTipo(TipoQuestaoEnum.MULTIPLA_ESCOLHA);
    q.setGabaritoIndice(gabaritoIndice);
    q.setPontos(new BigDecimal(pontos));
    return q;
}
```

---

## Notas e Edge Cases

- Usar `new BigDecimal("10")` para o divisor — não `BigDecimal.TEN` (diferença em escala interna)
- `UPLOAD_ARQUIVO` deve ser tratado como `DISSERTATIVA` — ignorado no cálculo
- O `entregaId` passado para `calcularNotaAutomatica` não é usado na lógica — pode ser qualquer `Long`

---

## Definition of Done

- [ ] Classe `AtividadeServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 18 testes)
- [ ] Testes passam com `./mvnw test -Dtest=AtividadeServiceTest`
