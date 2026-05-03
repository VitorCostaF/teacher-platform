# Plano de Implementação — turmas_detalhe-e-alunos

> **Task origem:** `docs/Tasks/turmas_detalhe-e-alunos.md`
> **Escopo:** Frontend — Turmas
> **Complexidade:** G
> **Sprint:** 2 — Gestão de Turmas
> **Depende de:** `turmas_lista-e-cards-plan.md`

---

## Contexto do Codebase

`turmas.service.ts`, `useTurmas`, `TurmaCard`, `Button`, `Input`, `Skeleton`, `ConfirmationModal` e `useConfirmationModal` já existem. TanStack Query e React Router configurados. Esta task cobre a aba "Alunos" da tela de detalhe da turma com modal de busca, importação CSV/XLSX e remoção.

---

## Dependências a Adicionar

```bash
# Parsing de CSV/XLSX no frontend para preview antes de enviar
npm install papaparse xlsx
npm install -D @types/papaparse
```

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/turmas/services/turmas.service.ts`:
```typescript
detalhe: (id: number) => apiClient.get<TurmaDetalhe>(`/turmas/${id}`).then(r => r.data),
listarAlunos: (turmaId: number) => apiClient.get<AlunoTurma[]>(`/turmas/${turmaId}/alunos`).then(r => r.data),
adicionarAluno: (turmaId: number, data: { alunoId?: string; email?: string }) =>
  apiClient.post(`/turmas/${turmaId}/alunos`, data).then(r => r.data),
removerAluno: (turmaId: number, alunoId: string) =>
  apiClient.delete(`/turmas/${turmaId}/alunos/${alunoId}`).then(r => r.data),
importarAlunos: (turmaId: number, file: File) => {
  const form = new FormData(); form.append('file', file)
  return apiClient.post<ImportacaoResult>(`/turmas/${turmaId}/alunos/importar`, form).then(r => r.data)
},
buscarAlunos: (query: string) =>
  apiClient.get<AlunoTurma[]>('/alunos/busca', { params: { q: query } }).then(r => r.data),
```

### Tipos adicionais

```typescript
export interface AlunoTurma { id: string; nome: string; email: string; avatarUrl: string; matriculadoEm: string }
export interface ImportacaoResult { importados: number; erros: Array<{ linha: number; motivo: string }> }
```

### Hooks

`frontend/src/features/turmas/hooks/useTurmaDetalhe.ts`
`frontend/src/features/turmas/hooks/useAlunosTurma.ts`

### Componentes

`frontend/src/features/turmas/components/TurmaTabNavigation.tsx`
- Abas: Alunos | Frequência | Atividades | Desempenho
- Usando `<Link>` do React Router com highlight da aba ativa
- Não recarrega a página ao trocar de aba

`frontend/src/features/turmas/components/AlunoListItem.tsx`
- Foto (avatar), nome, e-mail
- Botão "Remover" (ícone de lixo) que abre `ConfirmationModal`

`frontend/src/features/turmas/components/AdicionarAlunoModal.tsx`
- Campo de busca com debounce 300ms → chama `turmasService.buscarAlunos()`
- Lista de resultados com avatar, nome, e-mail + botão "Adicionar"
- Aba alternativa: "Convidar por e-mail" (input e-mail + botão enviar)
- Skeleton durante busca

`frontend/src/features/turmas/components/ImportarAlunosModal.tsx`
- Input `<input type="file" accept=".csv,.xlsx">` + drop zone
- Ao selecionar arquivo: parsear localmente com `papaparse` (CSV) ou `xlsx` (XLSX)
- Exibir preview: tabela com colunas Nome, E-mail + coluna de erro por linha
- Linhas com erro: fundo vermelho + mensagem
- Botão "Confirmar importação" → chama `turmasService.importarAlunos()`

### Página

`frontend/src/features/turmas/pages/TurmaDetalhePage.tsx`
- Rota: `/professor/turmas/:turmaId`
- `useParams` para `turmaId`
- Header com nome da turma + botão "Lançar Frequência" → navega para `/professor/turmas/:id/frequencia`
- `<TurmaTabNavigation>` com aba "Alunos" ativa por padrão
- Aba Alunos: lista com skeleton, botões "Adicionar Aluno" e "Importar via planilha"
- Estado vazio: ilustração + "Adicionar primeiro aluno"
- Remoção: `useConfirmationModal` com nível "alto" e descrição de consequências

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/professor/turmas/:turmaId` |

---

## Ordem de Implementação

```
1. Instalar papaparse e xlsx
2. Tipos adicionais em types.ts
3. Extensão de turmas.service.ts
4. useTurmaDetalhe, useAlunosTurma hooks
5. TurmaTabNavigation
6. AlunoListItem
7. AdicionarAlunoModal (busca + debounce)
8. ImportarAlunosModal (parse local + preview)
9. TurmaDetalhePage
10. Atualizar router
11. Testes: debounce 300ms, parse CSV/XLSX, preview de erros, modal de remoção
```

---

## Checklist de Validação

- [ ] Abas navegam sem reload da página
- [ ] Lista de alunos com skeleton
- [ ] Modal de busca com debounce 300ms
- [ ] Importação exibe preview com erros por linha
- [ ] Modal de remoção com confirmação explícita
- [ ] Estado vazio da aba
- [ ] Botão Lançar Frequência navega corretamente

---

## Resumo

- **8 arquivos** a criar (extensão service, hooks, 4 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Dependências a adicionar:** papaparse, xlsx
- **Complexidade mantida:** G
