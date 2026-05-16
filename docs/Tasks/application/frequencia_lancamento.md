# Frequência — Tela de Lançamento de Frequência

> **Escopo:** frequencia  
> **Tipo:** Frontend  
> **Complexidade estimada:** G  
> **Depende de:** `turmas_detalhe-e-alunos.md`, `backend-frequencia_endpoint-lancar.md`

---

## Contexto

O professor lança a presença/falta dos alunos de uma turma para uma data específica. O objetivo é fazer isso com o mínimo de cliques. É possível editar frequências já lançadas.

---

## O que deve ser implementado

- Rota `/professor/turmas/:turmaId/frequencia`
- Seletor de data (datepicker) limitado entre início da turma e hoje
- Ao trocar a data: verificar se já há registro via `GET /turmas/:id/frequencia?data=...`; se sim, pré-carregar e exibir badge "Frequência já lançada"
- Lista de alunos: toggle de 3 estados (Presente / Falta / Falta Justificada) + campo de observação expansível
- Botão "Marcar todos como presentes" no topo da lista
- Botão "Salvar" sticky no rodapé com contador "X de Y alunos registrados"
- Botão Salvar desabilitado até todos os alunos terem status definido
- Modal de confirmação ao trocar data com dados não salvos
- Autosave silencioso não se aplica aqui — salvar é sempre explícito

---

## Critérios de Aceite

- [ ] Seletor de data não permite datas futuras nem anteriores ao início da turma
- [ ] Ao selecionar data com registro existente, dados são pré-carregados
- [ ] Badge "já lançada" aparece para datas com registro existente
- [ ] Toggle funciona corretamente entre os 3 estados
- [ ] Botão "Marcar todos presentes" define todos como presente
- [ ] Botão Salvar exibe contador correto e é desabilitado com pendentes
- [ ] Modal de confirmação ao trocar data com alterações não salvas
- [ ] Após salvar com sucesso: toast + redirect para detalhe da turma

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02a-gestao-turmas.md`
- **Seção:** `Tela: Lançamento de Frequência`

---

## Notas e Edge Cases

- Alunos adicionados após uma data passada não aparecem retroativamente naquele dia
- Se a turma não tem aula no dia selecionado (baseado na grade), exibir aviso suave mas não bloquear

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testado em mobile (layout responsivo é crítico aqui)
- [ ] Code review realizado
