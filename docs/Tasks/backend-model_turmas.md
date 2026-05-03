# Backend Model — Tabelas de Turmas, Matrículas e Frequência

> **Escopo:** backend-model  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_usuario.md`

---

## Contexto

Migrations e models para a estrutura de turmas, matrículas de alunos, períodos letivos, frequência e vínculos de responsáveis.

---

## O que deve ser implementado

**Migrations:**
- `periodos_letivos` — id, escola_id, nome, inicio, fim, ativo (somente 1 ativo por escola via trigger/constraint)
- `turmas` — id, escola_id, periodo_letivo_id, professor_id (FK), nome, serie, disciplina, deletado_em (nullable)
- `matriculas` — id, turma_id, aluno_id, matriculado_em, removido_em. UNIQUE(turma_id, aluno_id)
- `vinculos_responsavel` — id, responsavel_id, aluno_id, parentesco. UNIQUE(responsavel_id, aluno_id)
- `registros_frequencia` — id, turma_id, aluno_id, data_aula, status (enum), observacao, lancado_por, lancado_em, editado_em. UNIQUE(turma_id, aluno_id, data_aula)
- `logs_auditoria` — id, escola_id, usuario_id, acao, entidade, entidade_id, dados_anteriores (JSONB), motivo, ip, criado_em. Append-only.

---

## Critérios de Aceite

- [ ] Migrations rodam sem erros e são reversíveis
- [ ] UNIQUE constraints funcionam (turma+aluno+data na frequência)
- [ ] Soft delete em turmas e matrículas funciona (deletado_em / removido_em)
- [ ] Logs de auditoria não têm UPDATE/DELETE habilitado (somente INSERT)
- [ ] Constraint de apenas 1 período letivo ativo por escola

---

## Especificação de Referência

- **Arquivo:** `06-modelos-de-dados.md`

---

## Definition of Done

- [ ] Migrations implementadas e testadas
- [ ] Models com relacionamentos
- [ ] Testes de migration up/down
- [ ] Code review realizado
