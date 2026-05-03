# Área do Aluno — Flashcards com Repetição Espaçada

> **Escopo:** aluno  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-ia_endpoint-gerar-prova.md`

---

## Contexto

Flashcards gerados por IA para revisão de conteúdo. Usam algoritmo de repetição espaçada para priorizar o que o aluno tem mais dificuldade.

---

## O que deve ser implementado

- Rota `/aluno/flashcards`
- Seletor de disciplina e tópico
- Card com pergunta na frente; toque/clique anima virada e revela resposta
- Botões pós-resposta: "Sabia" e "Não sabia" — registrar avaliação via `POST /aluno/flashcards/:id/avaliacao`
- Barra de progresso "X de Y cards revisados"
- Sessão pode ser encerrada a qualquer momento; progresso salvo
- Novos flashcards sugeridos baseados em desempenho recente

---

## Critérios de Aceite

- [ ] Animação de virada do card funciona em touch e click
- [ ] Botões "Sabia" / "Não sabia" registram avaliação corretamente
- [ ] Cards marcados "não sabia" reaparecem com mais frequência
- [ ] Progresso da sessão é salvo ao encerrar
- [ ] Estado vazio ("Nenhum flashcard disponível para este tópico") é exibido

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seção:** `Tela: Flashcards`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Animação de virada testada em mobile
- [ ] Code review realizado
