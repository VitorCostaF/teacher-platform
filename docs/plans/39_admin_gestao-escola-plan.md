# Plano de Implementação — admin_gestao-escola

> **Task origem:** `docs/Tasks/admin_gestao-escola.md`
> **Escopo:** Frontend — Área Administrativa
> **Complexidade:** G
> **Sprint:** 6 — Responsável e Administração
> **Depende de:** `backend-admin_endpoints-plan.md`

---

## Contexto do Codebase

`recharts`, `papaparse`, `xlsx`, `ConfirmationModal`, `Button`, `Input`, `Skeleton`, `useToast`, TanStack Query, React Router disponíveis. Esta é a maior área administrativa da plataforma.

---

## Arquivos a Criar

### Serviço

`frontend/src/features/admin/services/admin.service.ts`
```typescript
export const adminService = {
  getDashboard: () => apiClient.get<AdminDashboardData>('/admin/dashboard').then(r => r.data),
  listarProfessores: (params: FiltrosProfessor) => apiClient.get('/admin/professores', { params }).then(r => r.data),
  convidarProfessor: (data: ConvidarProfessorDto) => apiClient.post('/admin/professores/convidar', data),
  alterarStatusProfessor: (id: string, data: { ativo: boolean; motivo: string }) =>
    apiClient.patch(`/admin/professores/${id}/status`, data),
  importarProfessores: (file: File) => { const f = new FormData(); f.append('file', file); return apiClient.post('/admin/professores/importar', f) },
  listarAlunos: (params: FiltrosAluno) => apiClient.get('/admin/alunos', { params }).then(r => r.data),
  criarAluno: (data: CriarAlunoDto) => apiClient.post('/admin/alunos', data),
  transferirAluno: (id: string, novaTurmaId: number) => apiClient.patch(`/admin/alunos/${id}/turma`, { novaTurmaId }),
  importarAlunos: (file: File) => { const f = new FormData(); f.append('file', file); return apiClient.post('/admin/alunos/importar', f) },
  getConfiguracoes: () => apiClient.get('/admin/escola/configuracoes').then(r => r.data),
  salvarConfiguracoes: (data: ConfiguracoesDto) => apiClient.put('/admin/escola/configuracoes', data),
}
```

### Tipos

`frontend/src/features/admin/types.ts`
- `AdminDashboardData`, `ProfessorAdmin`, `AlunoAdmin`, `ConvidarProfessorDto`, `CriarAlunoDto`, `ConfiguracoesDto`

### Componentes — Visão Geral

`frontend/src/features/admin/components/KPICards.tsx`
- 5 cards: Total Alunos, Professores, Turmas, % Frequência Média, Média Notas

`frontend/src/features/admin/components/DesempenhoPorSerieChart.tsx`
- `BarChart` Recharts com barras agrupadas por série

`frontend/src/features/admin/components/AlertasConsolidadosTable.tsx`
- Tabela com filtros: tipo, turma, data
- Paginação client-side ou server-side

`frontend/src/features/admin/components/AtividadeRecenteFeed.tsx`
- Lista de últimas ações relevantes (convites, matrículas, publicações)

### Componentes — Gestão de Professores

`frontend/src/features/admin/components/ProfessoresTable.tsx`
- Colunas: nome, e-mail, disciplinas, status (ativo/inativo/convite pendente)
- Busca + filtro de status
- Clique na linha abre modal de detalhes

`frontend/src/features/admin/components/ConvidarProfessorModal.tsx`
- Campos: nome, e-mail, disciplinas (multiselect ou chips)
- Ao salvar: toast "Convite enviado para [email]"

`frontend/src/features/admin/components/ProfessorDetalhesModal.tsx`
- Dados do professor, turmas vinculadas
- Botão "Desativar" → `ConfirmationModal` com nível "alto" + campo motivo obrigatório

`frontend/src/features/admin/components/ImportarCSVModal.tsx`
- Upload de arquivo CSV
- Parsear localmente com papaparse → exibir tabela preview
- Linhas com erro em vermelho
- Botão "Confirmar importação"
- Resultado: "X importados, Y erros" em toast

### Componentes — Gestão de Alunos

`frontend/src/features/admin/components/AlunosTable.tsx`
- Colunas: nome, turmas, status, série
- Filtros: nome, turma, série, status

`frontend/src/features/admin/components/NovoAlunoModal.tsx`
- Dados pessoais, multiselect de turmas, seção de responsáveis (adicionar/remover dinamicamente)
- Validação com react-hook-form + zod

`frontend/src/features/admin/components/TransferirTurmaModal.tsx`
- Dropdown de nova turma
- Aviso: "O histórico da turma anterior será mantido"

### Componentes — Configurações

`frontend/src/features/admin/components/ConfiguracoesForm.tsx`
- Formulário: dados da escola (nome, CNPJ, logo)
- Regras pedagógicas: nota mínima, frequência mínima, sistema de avaliação
- Configurações de comunicação e integrações externas
- Botão "Salvar" com loading

### Páginas

`frontend/src/features/admin/pages/AdminVisaoGeralPage.tsx`
- Rota: `/admin/visao-geral`

`frontend/src/features/admin/pages/ProfessoresPage.tsx`
- Rota: `/admin/professores`

`frontend/src/features/admin/pages/AlunosAdminPage.tsx`
- Rota: `/admin/alunos`

`frontend/src/features/admin/pages/ConfiguracoesPage.tsx`
- Rota: `/admin/configuracoes`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | 4 novas rotas `/admin/**` dentro de ProtectedRoute com guarda de perfil ADMIN |

---

## Ordem de Implementação

```
1. types.ts + admin.service.ts
2. KPICards
3. DesempenhoPorSerieChart
4. AlertasConsolidadosTable
5. ImportarCSVModal (genérico, usado por professores e alunos)
6. ConvidarProfessorModal + ProfessorDetalhesModal
7. ProfessoresTable
8. NovoAlunoModal + TransferirTurmaModal
9. AlunosTable
10. ConfiguracoesForm
11. 4 páginas admin
12. Atualizar router
13. Testes: tabela com 500+ alunos (performance), importação CSV com erros, desativação com motivo
```

---

## Checklist de Validação

- [ ] KPIs carregam corretamente
- [ ] Convite de professor: "Convite enviado" na UI
- [ ] Desativação requer confirmação + motivo
- [ ] Importação CSV exibe erros por linha
- [ ] Modal aluno suporta múltiplos responsáveis
- [ ] Configurações persistem após reload

---

## Resumo

- **16 arquivos** a criar (service, types, 10 componentes, 4 páginas)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova** (papaparse, xlsx, recharts já instalados)
- **Complexidade mantida:** G
