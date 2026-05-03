# Backend Frequência — Endpoints de Lançamento e Histórico

> **Escopo:** backend-frequencia  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_turmas.md`

---

## Contexto

Endpoints para lançar, editar e consultar frequência. Inclui a lógica de alertas automáticos para responsáveis quando o aluno atinge limiares de falta.

---

## O que deve ser implementado

- `GET /turmas/:id/frequencia?data=YYYY-MM-DD` — retornar registro existente ou `null`
- `POST /turmas/:id/frequencia` — criar novo registro com array de `{ alunoId, status, observacao }`
- `PUT /turmas/:id/frequencia/:frequenciaId` — editar registro existente
- `GET /turmas/:id/alunos/:alunoId/frequencia` — histórico com resumo, calendário e percentual
- Após cada POST/PUT: calcular percentual de frequência do aluno; se < 75% ou 3 faltas consecutivas, disparar notificação para responsáveis (via fila assíncrona)

---

## Critérios de Aceite

- [ ] GET retorna registro existente ou null corretamente
- [ ] POST cria registro com todos os alunos da turma
- [ ] PUT atualiza registro mantendo histórico de edição
- [ ] Histórico calcula percentual corretamente: `(presenças / total aulas até hoje) * 100`
- [ ] Alerta de falta é enfileirado corretamente após POST/PUT
- [ ] UNIQUE constraint (turma, aluno, data) é respeitada

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02a-gestao-turmas.md`
- **Seções:** `Tela: Lançamento de Frequência > Chamadas de API` e `Alertas Automáticos de Falta`

---

## Definition of Done

- [ ] Endpoints implementados com validações
- [ ] Lógica de alerta com fila assíncrona
- [ ] Testes unitários e de integração
- [ ] Code review realizado
