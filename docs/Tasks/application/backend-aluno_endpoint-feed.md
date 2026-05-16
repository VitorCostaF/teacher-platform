# Backend Aluno — Endpoints de Feed e Desempenho Pessoal

> **Escopo:** backend-aluno  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_avaliacoes.md`

---

## Contexto

Endpoints que alimentam o feed do aluno, seu desempenho pessoal e sistema de gamificação.

---

## O que deve ser implementado

- `GET /aluno/feed` — urgentes (prazo < 24h), para fazer, novos conteúdos, recomendações da IA
- `GET /aluno/desempenho` — resumo, por disciplina com tendência, evolução de notas, conquistas
- Lógica de recomendação da IA: identificar tópicos com menor acerto e sugerir conteúdos relacionados
- Lógica de gamificação: calcular pontos (entrega no prazo, acesso a conteúdos, performance) e conquistas (streaks, top 3, sem faltas no mês)
- `GET /aluno/flashcards?disciplina=:id` — flashcards priorizados por repetição espaçada
- `POST /aluno/flashcards/:cardId/avaliacao` — registrar resultado e recalcular `proxima_revisao`

---

## Critérios de Aceite

- [ ] Feed retorna seções corretamente categorizadas
- [ ] Recomendações baseadas em tópicos com < X% de acerto
- [ ] Conquistas calculadas corretamente para cada critério
- [ ] Flashcards ordenados por `proxima_revisao` (repetição espaçada SM-2 ou similar)
- [ ] Aluno só vê avaliações de suas turmas

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seções:** `Chamadas de API` de cada tela

---

## Definition of Done

- [ ] Endpoints implementados com permissões corretas
- [ ] Algoritmo de repetição espaçada implementado
- [ ] Testes unitários para lógica de gamificação
- [ ] Code review realizado
