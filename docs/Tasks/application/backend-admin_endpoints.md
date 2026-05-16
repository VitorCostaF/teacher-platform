# Backend Admin — Endpoints Administrativos

> **Escopo:** backend-admin  
> **Tipo:** Backend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-model_usuario.md`, `backend-dashboard_endpoint-professor.md`

---

## Contexto

Endpoints de gestão administrativa. Todas as ações destrutivas devem ser registradas no log de auditoria.

---

## O que deve ser implementado

- `GET /admin/dashboard` — KPIs, desempenho por série, frequência por turma, alertas, atividade recente
- `GET /admin/professores` — lista paginada com filtros
- `POST /admin/professores/convidar` — gerar token de convite, enviar e-mail, salvar como usuário inativo
- `PATCH /admin/professores/:id/status` — ativar/desativar com registro de motivo em log_auditoria
- `POST /admin/professores/importar` — importar CSV com validação e relatório de erros
- `GET /admin/alunos` — lista paginada com filtros (nome, turma, série, status)
- `POST /admin/alunos` — criar aluno + matrículas + responsáveis + disparar convites
- `PATCH /admin/alunos/:id/turma` — transferir aluno (soft delete matrícula antiga, nova matrícula)
- `POST /admin/alunos/importar` — importar CSV
- `GET/PUT /admin/escola/configuracoes` — configurações da escola
- Toda ação destrutiva deve escrever em `logs_auditoria`

---

## Critérios de Aceite

- [ ] 403 para qualquer rota admin acessada por professor ou aluno
- [ ] Coordenador tem acesso read-only (GET) mas não pode criar/editar/desativar
- [ ] Log de auditoria escrito em todas as ações destrutivas com `dados_anteriores` e `motivo`
- [ ] Importação CSV retorna { importados: N, erros: [{linha, motivo}] }
- [ ] Convite de professor tem validade de 72h

---

## Especificação de Referência

- **Arquivo:** `05-area-administrativa.md`
- **Seção:** `Regras de Negócio — Área Administrativa`

---

## Notas e Edge Cases

- Log de auditoria é append-only — nunca atualizar ou deletar registros
- Desativar professor não deve interromper turmas em andamento — apenas bloquear o login

---

## Definition of Done

- [ ] Endpoints com todas as validações de permissão
- [ ] Log de auditoria testado para todas as ações destrutivas
- [ ] Testes de importação com arquivos válidos e inválidos
- [ ] Code review realizado
