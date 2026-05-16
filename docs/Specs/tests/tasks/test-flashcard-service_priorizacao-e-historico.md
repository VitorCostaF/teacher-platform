# Testes Unitários — FlashcardService: Priorização e Histórico

> **Escopo:** Backend — `FlashcardService` (getFlashcardsPriorizados)  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** `test-flashcard-service_algoritmo-sm2.md` (mesma classe)

---

## Contexto

`getFlashcardsPriorizados` seleciona quais flashcards o aluno deve revisar hoje, filtrando por data de próxima revisão e opcionalmente por turma. É a entrada do fluxo de estudo do aluno — se retornar flashcards errados, o aluno estuda fora de ordem ou não estuda o que precisa.

---

## O que deve ser implementado

Adicionar à `FlashcardServiceTest` os cenários do método `getFlashcardsPriorizados`. O método é `readOnly` e depende apenas de consultas ao repositório — todos os repositórios devem ser mockados.

**Cenários a cobrir:**
- `turmaId` informado → filtra apenas flashcards dessa turma
- `turmaId = null` → usa todas as turmas do aluno via matrícula
- Nenhum estado pendente → lista vazia
- Estados fora do prazo (próxima revisão no futuro) → não retornados (filtro do repositório)

---

## Critérios de Aceite

- [ ] Com `turmaId` informado, retorna apenas flashcards da turma informada
- [ ] Com `turmaId = null`, busca turmas via `matriculaRepository.findByAlunoIdAndRemovidoEmIsNull`
- [ ] Com `turmaId = null`, retorna flashcards das turmas encontradas nas matrículas
- [ ] Com estados de turmas diferentes e `turmaId` específico, exclui as outras turmas
- [ ] Repositório chamado com `proximaRevisao <= today`
- [ ] Retorna lista vazia quando `estadoSm2Repository` retorna vazio
- [ ] Mapeamento correto: `FlashcardResponse` contém `id`, `pergunta`, `resposta`, `disciplina`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/FlashcardService.md`
- **Seção:** `Método: getFlashcardsPriorizados`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/FlashcardServiceTest.java`

O mock do `estadoSm2Repository` deve retornar estados com a `Turma` configurada para filtrar corretamente:
```java
var turma = new Turma(); turma.setId(1L);
var flashcard = new Flashcard(); flashcard.setTurma(turma);
var estado = new FlashcardEstadoSm2(); estado.setFlashcard(flashcard);
```

---

## Notas e Edge Cases

- A filtragem por turma ocorre em memória (stream filter) após a query — o mock do repositório deve retornar estados de várias turmas para testar o filtro
- Com `turmaId = null`, o método consulta matrículas ativas (`removidoEmIsNull`)

---

## Definition of Done

- [ ] Cenários de priorização adicionados à `FlashcardServiceTest` (mínimo 7 testes)
- [ ] Testes passam junto com os de SM-2
