# Índice de Tasks — Plataforma Educacional com IA

> Total: **35 tasks** organizadas por escopo e ordem sugerida de implementação.

---

## Todas as Tasks por Escopo

### 🔧 Backend — Modelos de Dados (implementar primeiro)

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| Tabelas de Usuário e Sessão | `backend-model_usuario.md` | M | — |
| Tabelas de Turmas, Matrículas e Frequência | `backend-model_turmas.md` | M | backend-model_usuario |
| Tabelas de Avaliações, Entregas e Conteúdos | `backend-model_avaliacoes.md` | M | backend-model_turmas |

---

### 🔐 Autenticação

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoint POST /auth/login | `backend-auth_endpoint-post-login.md` | Backend | M | backend-model_usuario |
| Endpoints Refresh e Logout | `backend-auth_endpoint-refresh-logout.md` | Backend | M | backend-auth_endpoint-post-login |
| Endpoints Convite e Recuperação de Senha | `backend-auth_endpoint-convite.md` | Backend | M | backend-model_usuario |
| Tela de Login — Estrutura | `login_estrutura-do-formulario.md` | Frontend | P | — |
| Tela de Login — Validação e Erros | `login_validacao-e-estados-de-erro.md` | Frontend | M | login_estrutura |
| Tela de Login — Integração e Redirect | `login_redirecionamento-por-perfil.md` | Frontend | M | login_validacao, backend-auth_post-login |
| Tela de Primeiro Acesso (Convite) | `primeiro-acesso_tela-convite.md` | Frontend | M | backend-auth_convite |
| Tela de Recuperação de Senha | `recuperar-senha_fluxo-completo.md` | Frontend | M | backend-auth_convite |

---

### 👨‍🏫 Área do Professor — Gestão de Turmas

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoints de Turmas e Alunos | `backend-turmas_endpoint-listar.md` | Backend | M | backend-model_turmas |
| Endpoints de Frequência | `backend-frequencia_endpoint-lancar.md` | Backend | M | backend-model_turmas |
| Tela Lista de Turmas | `turmas_lista-e-cards.md` | Frontend | M | backend-turmas_endpoint-listar |
| Tela Detalhe da Turma e Alunos | `turmas_detalhe-e-alunos.md` | Frontend | G | turmas_lista |
| Tela Lançamento de Frequência | `frequencia_lancamento.md` | Frontend | G | turmas_detalhe, backend-frequencia |

---

### 🤖 Área do Professor — Criação com IA

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoints de IA (gerar prova/grade/flashcards) | `backend-ia_endpoint-gerar-prova.md` | Backend | G | backend-model_avaliacoes |
| Endpoints de Avaliações (CRUD e publicação) | `backend-avaliacoes_endpoint-publicar.md` | Backend | G | backend-model_avaliacoes |
| Hub de Criação e Gerador de Provas | `criacao-ia_gerador-provas.md` | Frontend | G | backend-ia, turmas_lista |
| Gerador de Atividades | `criacao-ia_gerador-atividades.md` | Frontend | M | criacao-ia_gerador-provas |
| Grade de Aulas e Sugestão de Conteúdos | `criacao-ia_grade-e-sugestoes.md` | Frontend | M | backend-ia |
| Revisão e Publicação | `criacao-ia_revisao-publicacao.md` | Frontend | M | criacao-ia_gerador-provas, backend-avaliacoes |

---

### 📊 Área do Professor — Dashboard de Desempenho

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoints de Dashboard e Relatórios | `backend-dashboard_endpoint-professor.md` | Backend | G | backend-model_avaliacoes, backend-frequencia |
| Dashboard Visão Geral | `dashboard_professor-visao-geral.md` | Frontend | G | backend-dashboard |
| Desempenho por Turma e Perfil do Aluno | `dashboard_desempenho-turma-e-aluno.md` | Frontend | G | backend-dashboard, turmas_detalhe |

---

### 👨‍🎓 Área do Aluno

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoints de Feed e Desempenho Pessoal | `backend-aluno_endpoint-feed.md` | Backend | M | backend-model_avaliacoes |
| Endpoints de Entregas | `backend-aluno_endpoint-entregas.md` | Backend | G | backend-model_avaliacoes |
| Feed de Conteúdos | `aluno_feed-conteudos.md` | Frontend | M | backend-aluno_feed |
| Realização de Atividade | `aluno_realizacao-atividade.md` | Frontend | G | backend-aluno_entregas |
| Realização de Prova com Timer | `aluno_realizacao-prova-timer.md` | Frontend | G | aluno_realizacao-atividade |
| Resultado e Feedback | `aluno_resultado-feedback.md` | Frontend | M | aluno_realizacao-atividade |
| Flashcards com Repetição Espaçada | `aluno_flashcards.md` | Frontend | M | backend-ia |
| Meu Desempenho | `aluno_desempenho-pessoal.md` | Frontend | M | backend-aluno_feed |

---

### 👨‍👩‍👧 Área do Responsável

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoints do Responsável | `backend-responsavel_endpoints.md` | Backend | M | backend-dashboard |
| Painel, Boletim e Frequência | `responsavel_painel-e-boletim.md` | Frontend | M | backend-responsavel |

---

### 🏫 Área Administrativa

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Endpoints Administrativos | `backend-admin_endpoints.md` | Backend | G | backend-model_usuario, backend-dashboard |
| Gestão de Escola, Professores e Alunos | `admin_gestao-escola.md` | Frontend | G | backend-admin |

---

### 🌐 Comportamentos Globais

| Task | Arquivo | Tipo | Complexidade | Depende de |
|------|---------|------|-------------|-----------|
| Sessão Expirada e Refresh Automático | `global_sessao-expirada.md` | Frontend | M | login_redirect, backend-auth_refresh |
| Suporte Offline e PWA | `global_offline-e-pwa.md` | Frontend | G | aluno_realizacao-atividade |
| Tratamento de Erros e Feedback Visual | `global_tratamento-erros-servidor.md` | Frontend | M | login_validacao |
| Padrão de Ações Destrutivas | `global_acoes-destrutivas.md` | Frontend | P | — |
| Sistema de Notificações Push | `global_notificacoes-push.md` | Fullstack | G | global_offline, backend-frequencia |

---

## Ordem Sugerida de Implementação

### Sprint 0 — Fundação (sem isso, nada funciona)
1. `backend-model_usuario`
2. `backend-model_turmas`
3. `backend-model_avaliacoes`
4. `global_tratamento-erros-servidor`
5. `global_acoes-destrutivas`

### Sprint 1 — Autenticação
6. `backend-auth_endpoint-post-login`
7. `backend-auth_endpoint-refresh-logout`
8. `backend-auth_endpoint-convite`
9. `login_estrutura-do-formulario`
10. `login_validacao-e-estados-de-erro`
11. `login_redirecionamento-por-perfil`
12. `global_sessao-expirada`
13. `primeiro-acesso_tela-convite`
14. `recuperar-senha_fluxo-completo`

### Sprint 2 — Gestão de Turmas e Frequência
15. `backend-turmas_endpoint-listar`
16. `backend-frequencia_endpoint-lancar`
17. `turmas_lista-e-cards`
18. `turmas_detalhe-e-alunos`
19. `frequencia_lancamento`

### Sprint 3 — Criação com IA
20. `backend-ia_endpoint-gerar-prova`
21. `backend-avaliacoes_endpoint-publicar`
22. `criacao-ia_gerador-provas`
23. `criacao-ia_gerador-atividades`
24. `criacao-ia_grade-e-sugestoes`
25. `criacao-ia_revisao-publicacao`

### Sprint 4 — Área do Aluno
26. `backend-aluno_endpoint-entregas`
27. `backend-aluno_endpoint-feed`
28. `aluno_feed-conteudos`
29. `aluno_realizacao-atividade`
30. `aluno_realizacao-prova-timer`
31. `aluno_resultado-feedback`
32. `aluno_flashcards`
33. `aluno_desempenho-pessoal`

### Sprint 5 — Dashboards e Relatórios
34. `backend-dashboard_endpoint-professor`
35. `dashboard_professor-visao-geral`
36. `dashboard_desempenho-turma-e-aluno`

### Sprint 6 — Responsável e Administração
37. `backend-responsavel_endpoints`
38. `responsavel_painel-e-boletim`
39. `backend-admin_endpoints`
40. `admin_gestao-escola`

### Sprint 7 — Polimento e Infraestrutura
41. `global_offline-e-pwa`
42. `global_notificacoes-push`

---

## Legenda

| Complexidade | Estimativa |
|-------------|-----------|
| **P** (Pequena) | ~0.5 dia |
| **M** (Média) | ~1-2 dias |
| **G** (Grande) | ~3-5 dias |
