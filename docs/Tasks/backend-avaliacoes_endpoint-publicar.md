# Backend Avaliações — Endpoints de CRUD e Publicação

> **Escopo:** backend-avaliacoes  
> **Tipo:** Backend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-model_avaliacoes.md`

---

## Contexto

Endpoints completos para gerenciar provas e atividades: criar rascunho, editar, publicar/agendar e consultar.

---

## O que deve ser implementado

- `POST /provas/rascunho` — salvar rascunho com config e questões
- `PUT /provas/:id` — atualizar rascunho
- `GET /provas/:id/preview` — retornar prova como aluno veria (questões embaralhadas se configurado)
- `POST /provas/:id/publicar` — publicar com config de data, embaralhamento, gabarito, peso, turmas destinatárias. Muda status para "publicada" ou "agendada". Dispara notificações para alunos.
- `GET /provas/:id` — detalhes (professor vê gabarito, aluno não)
- Regra: prova publicada não pode ter questões removidas, apenas adicionadas

---

## Critérios de Aceite

- [ ] Rascunho salvo e recuperável
- [ ] Publicação com data futura cria status "agendada"
- [ ] Preview não expõe gabarito
- [ ] Questões não podem ser removidas de prova publicada
- [ ] Notificações enfileiradas ao publicar
- [ ] Permissão 403 se professor tentar publicar prova de outro professor

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02b-criacao-com-ia.md`
- **Seção:** `Tela: Revisão e Publicação > Chamadas de API`

---

## Definition of Done

- [ ] Endpoints implementados com todas as regras de negócio
- [ ] Testes unitários e de integração
- [ ] Code review realizado
