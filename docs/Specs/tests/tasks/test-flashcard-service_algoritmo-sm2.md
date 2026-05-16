# Testes Unitários — FlashcardService: Algoritmo SM-2

> **Escopo:** Backend — `FlashcardService` (algoritmo de repetição espaçada)  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

O algoritmo SM-2 (Spaced Repetition) é o núcleo do sistema de flashcards. Ele determina quando cada flashcard deve ser revisado novamente com base no histórico de acertos do aluno. Uma falha nessa lógica afeta diretamente a efetividade pedagógica do produto. O método `aplicarSm2` é private — testado indiretamente via `registrarAvaliacao`.

---

## O que deve ser implementado

Criar `FlashcardServiceTest` com foco nos cenários do algoritmo SM-2. Para cada chamada a `registrarAvaliacao`, verificar o estado final de `FlashcardEstadoSm2` salvo (fator de facilidade, intervalo de dias, próxima revisão, total de revisões).

**Cenários a cobrir:**
- `sabia = true`: fator aumenta +0.1, intervalo = `max(1, round(intervalo * novoFator))`
- `sabia = false`: fator diminui -0.2 (mínimo 1.3), intervalo resetado para 1
- Fator não desce abaixo de 1.3 em nenhuma situação
- `totalRevisoes` incrementado em 1 sempre
- `proximaRevisao = today + intervalo`

---

## Critérios de Aceite

- [ ] `sabia=true` com fator=2.5, intervalo=1 → fator=2.6, intervalo=3
- [ ] `sabia=true` com fator=2.5, intervalo=3 → fator=2.6, intervalo=8
- [ ] `sabia=true` com fator=2.5, intervalo=8 → fator=2.6, intervalo=21
- [ ] `sabia=true` com fator=3.0, intervalo=10 → fator=3.1, intervalo=31
- [ ] `sabia=false` com fator=2.5, intervalo=10 → fator=2.3, intervalo=1
- [ ] `sabia=false` com fator=1.5 → fator fixado em 1.3 (mínimo), intervalo=1
- [ ] `sabia=false` com fator=1.3 → fator permanece 1.3, intervalo=1
- [ ] `sabia=false` com fator=1.4 → fator fixado em 1.3 (1.4 - 0.2 = 1.2 → mínimo 1.3)
- [ ] `totalRevisoes` vai de 0 para 1 independente do resultado
- [ ] `totalRevisoes` vai de 5 para 6 independente do resultado
- [ ] `proximaRevisao = LocalDate.now().plusDays(intervalo)` para `sabia=true`
- [ ] `proximaRevisao = LocalDate.now().plusDays(1)` para `sabia=false`
- [ ] Flashcard inexistente lança `UnauthorizedException`
- [ ] Estado inexistente → cria novo com `alunoId` e `flashcard` corretos
- [ ] Estado existente → atualiza o mesmo objeto
- [ ] `estadoSm2Repository.save` chamado 1 vez
- [ ] `progressoRepository.save` chamado com `resultado=FACIL` quando `sabia=true`
- [ ] `progressoRepository.save` chamado com `resultado=DIFICIL` quando `sabia=false`
- [ ] Aluno não encontrado no histórico lança `UnauthorizedException`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/FlashcardService.md`
- **Seções:** `Algoritmo SM-2`, `Método: registrarAvaliacao`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/FlashcardServiceTest.java`

**Helper:**
```java
private FlashcardEstadoSm2 estadoComFator(double fator, int intervalo) {
    var e = new FlashcardEstadoSm2();
    e.setFatorFacilidade(new BigDecimal(String.valueOf(fator)));
    e.setIntervaloDias(intervalo);
    e.setTotalRevisoes(0);
    return e;
}
```

**Atenção com BigDecimal:** Usar `new BigDecimal("2.5")` (String) e não `new BigDecimal(2.5)` (double) para evitar imprecisão de ponto flutuante nos asserts.

---

## Notas e Edge Cases

- O fator mínimo é **exatamente 1.3** — usar `compareTo` e não `equals` com BigDecimal
- O intervalo usa `Math.round` com o novo fator já atualizado — não o anterior
- O intervalo mínimo após `sabia=true` é 1 (`max(1, ...)`)

---

## Definition of Done

- [ ] Classe `FlashcardServiceTest` criada
- [ ] Todos os cenários SM-2 da spec cobertos (mínimo 19 testes)
- [ ] Testes passam com `./mvnw test -Dtest=FlashcardServiceTest`
