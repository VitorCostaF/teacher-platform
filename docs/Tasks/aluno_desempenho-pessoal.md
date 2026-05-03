# Área do Aluno — Tela Meu Desempenho

> **Escopo:** aluno  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-aluno_endpoint-feed.md`

---

## Contexto

O aluno acompanha seu próprio progresso: média geral, desempenho por disciplina, histórico de notas e conquistas de gamificação.

---

## O que deve ser implementado

- Rota `/aluno/desempenho`
- Cards de resumo: média global, total de atividades entregues, % de frequência
- Cards por disciplina: média, tendência (↑↓), próxima avaliação
- Gráfico de linha de evolução de notas com seletor de disciplina
- Seção de conquistas: badges de gamificação com nome e critério
- Atalho para flashcards dos tópicos com menor acerto

---

## Critérios de Aceite

- [ ] Média global calculada ponderando todas as disciplinas
- [ ] Tendência por disciplina exibida corretamente
- [ ] Gráfico muda ao trocar disciplina no seletor
- [ ] Badges de conquistas exibidos com critério visível no hover/tap
- [ ] Link para flashcards abre na disciplina/tópico correto

---

## Especificação de Referência

- **Arquivo:** `03-area-aluno.md`
- **Seção:** `Tela: Meu Desempenho`

---

## Definition of Done

- [ ] Código implementado e funcionando
- [ ] Testado em mobile
- [ ] Code review realizado
