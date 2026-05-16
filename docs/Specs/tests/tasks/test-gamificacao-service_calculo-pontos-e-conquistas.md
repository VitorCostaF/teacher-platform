# Testes Unitários — GamificacaoService: Cálculo de Pontos e Conquistas

> **Escopo:** Backend — `GamificacaoService`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`GamificacaoService` implementa o sistema de pontuação e conquistas da plataforma. Pontos são calculados com base em entregas e revisões de flashcards. Conquistas são concedidas quando o aluno atinge metas específicas. A idempotência das conquistas é crítica — duplicatas degradam a experiência do aluno.

---

## O que deve ser implementado

Criar `GamificacaoServiceTest` cobrindo `calcularPontos` e `verificarConquistas`. Todos os repositórios são mockados — os testes validam a lógica de agregação e as regras de threshold.

**Cenários `calcularPontos`:**
- Entrega no prazo sem nota: +10 pts
- Entrega no prazo com nota ≥ 7: +10 +20 = 30 pts
- Entrega atrasada: +3 pts
- Nota < 7: sem bônus de nota
- Nota exatamente 7.0: bônus incluído
- Entregas RASCUNHO/NAO_INICIADA: ignoradas
- Flashcards: soma de `totalRevisoes`

**Cenários `verificarConquistas`:**
- DEDICADO: 10+ entregas no prazo (idempotente)
- EXCELENCIA: 5+ notas ≥ 9.0 (nota exata 9.0 inclusa)
- FLASHMASTER: 50+ revisões totais (soma)
- Conquista já existente → não duplica

---

## Critérios de Aceite

- [ ] 0 entregas e 0 flashcards → 0 pontos
- [ ] 1 entrega no prazo, sem nota → 10 pontos
- [ ] 1 entrega no prazo, nota=8.0 → 30 pontos
- [ ] 1 entrega atrasada, sem nota → 3 pontos
- [ ] 1 entrega atrasada, nota=9.0 → 23 pontos
- [ ] Nota=6.9 → sem bônus (apenas 10 pts)
- [ ] Nota=7.0 → bônus incluído (30 pts)
- [ ] Entrega RASCUNHO com nota=10 → 0 pontos
- [ ] 5 revisões de flashcard (soma de totalRevisoes) → 5 pontos
- [ ] Mix de entregas + flashcards: soma correta
- [ ] DEDICADO criado quando ≥ 10 entregas no prazo e conquista não existe
- [ ] DEDICADO **não** criado quando conquista já existe (`existsByAlunoIdAndTipo = true`)
- [ ] DEDICADO **não** criado com < 10 entregas no prazo
- [ ] DEDICADO não conta entregas atrasadas nem RASCUNHO
- [ ] EXCELENCIA criado quando ≥ 5 notas ≥ 9.0 e não existe
- [ ] Nota 9.0 exata conta para EXCELENCIA; nota 8.9 não conta
- [ ] Entregas sem nota não contam para EXCELENCIA
- [ ] FLASHMASTER criado quando soma ≥ 50 revisões e não existe
- [ ] FLASHMASTER **não** criado com soma < 50
- [ ] `conquistaRepository.save` chamado com `tipo`, `descricao` e `obtidaEm` corretos

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/GamificacaoService.md`
- **Seções:** `calcularPontos`, `verificarConquistas`, `getConquistas`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/GamificacaoServiceTest.java`

**Helper:**
```java
private Entrega entregaFake(StatusEntregaEnum status, boolean atrasada, String nota) {
    var e = new Entrega();
    e.setStatus(status);
    e.setEntregaAtrasada(atrasada);
    e.setNotaFinal(nota != null ? new BigDecimal(nota) : null);
    return e;
}
```

---

## Notas e Edge Cases

- A pontuação considera `notaFinal`, não `notaAutomatica`
- Para flashcards, o repositório recebe `LocalDate.now().plusYears(10)` como limite — o mock deve responder corretamente
- Usar `BigDecimal("7")` e `compareTo >= 0` para checar >= 7.0

---

## Definition of Done

- [ ] Classe `GamificacaoServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 20 testes)
- [ ] Testes passam com `./mvnw test -Dtest=GamificacaoServiceTest`
