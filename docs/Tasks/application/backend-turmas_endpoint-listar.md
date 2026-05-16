# Backend Turmas — Endpoints de Listagem e CRUD de Turmas

> **Escopo:** backend-turmas  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_turmas.md`

---

## Contexto

Endpoints para listar, criar, editar e encerrar turmas. Professor vê apenas suas turmas; Admin vê todas da escola.

---

## O que deve ser implementado

- `GET /professor/turmas?periodo=:id` — lista turmas do professor autenticado (ou todas se admin), com contagem de alunos e pendências
- `GET /turmas/:id` — detalhes da turma (verifica permissão)
- `POST /admin/turmas` — criar turma (somente Admin)
- `PUT /admin/turmas/:id` — editar turma (somente Admin)
- `PATCH /admin/turmas/:id/encerrar` — soft delete / encerrar turma (somente Admin)
- `GET /turmas/:id/alunos` — listar alunos da turma
- `POST /turmas/:id/alunos` — adicionar aluno (`{ alunoId }` ou `{ email }` para convite)
- `DELETE /turmas/:id/alunos/:alunoId` — remover aluno (soft delete na matrícula)
- `POST /turmas/:id/alunos/importar` — importar via CSV/XLSX, retornar `{ importados, erros[] }`

---

## Critérios de Aceite

- [ ] Professor vê apenas suas turmas; Admin vê todas da escola
- [ ] `pendencias` calculadas corretamente (frequências e atividades)
- [ ] Permissão 403 para professor tentando acessar turma de outro professor
- [ ] Importação retorna lista de erros por linha sem interromper as linhas válidas
- [ ] Turma encerrada fica em somente leitura

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02a-gestao-turmas.md`
- **Seção:** `Chamadas de API` e `Regras de Negócio — Turmas`

---

## Definition of Done

- [ ] Endpoints implementados com todos os casos de erro
- [ ] Testes unitários e de integração
- [ ] Code review realizado
