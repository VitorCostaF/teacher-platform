# Backend Responsável — Endpoints de Acompanhamento

> **Escopo:** backend-responsavel  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-dashboard_endpoint-professor.md`

---

## Contexto

Endpoints read-only para responsáveis. Usam os mesmos dados do professor mas com filtro de permissão (responsável só vê dados do seu filho) e sem campos privados (observações do professor).

---

## O que deve ser implementado

- `GET /responsavel/alunos` — listar alunos vinculados ao responsável autenticado
- `GET /responsavel/alunos/:alunoId/painel` — resumo, alertas ativos, próxima prova. Validar que `:alunoId` é filho do responsável.
- `GET /responsavel/alunos/:alunoId/boletim?periodo=:p` — notas por disciplina e período, sem observações privadas
- `GET /responsavel/alunos/:alunoId/frequencia` — resumo, calendário, lista de faltas
- `GET /responsavel/alunos/:alunoId/calendario` — provas futuras e passadas com nota (se disponível)
- Sistema de alertas: consultar alertas pendentes para o responsável

---

## Critérios de Aceite

- [ ] 403 se responsável tentar acessar dados de aluno que não é seu filho
- [ ] Observações privadas do professor não aparecem nas respostas
- [ ] Alertas calculados corretamente (frequência < 75%, faltas consecutivas, etc.)
- [ ] Boletim não expõe notas antes do professor lançar

---

## Especificação de Referência

- **Arquivo:** `04-area-responsavel.md`
- **Seção:** `Regras de Negócio — Responsável`

---

## Definition of Done

- [ ] Endpoints implementados com validação de permissão rigorosa
- [ ] Testes de permissão (garantir isolamento de dados)
- [ ] Code review realizado
