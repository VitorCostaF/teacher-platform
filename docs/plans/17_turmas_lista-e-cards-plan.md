# Plano de Implementação — turmas_lista-e-cards

> **Task origem:** `docs/Tasks/turmas_lista-e-cards.md`
> **Escopo:** Frontend — Turmas
> **Complexidade:** M
> **Sprint:** 2 — Gestão de Turmas
> **Depende de:** `backend-turmas_endpoint-listar-plan.md`

---

## Contexto do Codebase

`apiClient`, `authStore` (getCurrentUser para checar se é Admin), `Skeleton`, `Button`, `Input`, `ProtectedRoute` já existem. TanStack Query v5 instalado. React Router configurado.

---

## Arquivos a Criar

### Serviço

`frontend/src/features/turmas/services/turmas.service.ts`
```typescript
export const turmasService = {
  listar: (periodoId: number) =>
    apiClient.get<TurmaResumo[]>('/professor/turmas', { params: { periodo: periodoId } }).then(r => r.data),
  listarPeriodos: () =>
    apiClient.get<PeriodoLetivo[]>('/periodos-letivos').then(r => r.data),
}
```

### Tipos

`frontend/src/features/turmas/types.ts`
```typescript
export interface TurmaResumo {
  id: number; nome: string; disciplina: string; totalAlunos: number
  proximaAula: string | null
  pendencias: { frequenciasNaoLancadas: number; atividadesNaoCorrigidas: number }
}
export interface PeriodoLetivo { id: number; nome: string; ativo: boolean }
```

### Hooks

`frontend/src/features/turmas/hooks/useTurmas.ts`
```typescript
export function useTurmas(periodoId: number) {
  return useQuery({
    queryKey: ['turmas', periodoId],
    queryFn: () => turmasService.listar(periodoId),
  })
}
```

### Componentes

`frontend/src/features/turmas/components/TurmaCard.tsx`
- Props: `turma: TurmaResumo`
- Renderiza: nome, disciplina, `totalAlunos alunos`, próxima aula
- Badges de pendências: aparecem **apenas quando > 0**
  - Badge amarelo: "X frequência(s) pendente(s)"
  - Badge vermelho: "X atividade(s) para corrigir"
- Clicável: navega para `/professor/turmas/:id`
- Hover: elevação leve (shadow Tailwind)

`frontend/src/features/turmas/components/TurmaCardSkeleton.tsx`
- Placeholder com shimmer (usa `Skeleton` base)
- Mesmo tamanho e proporções do `TurmaCard`

`frontend/src/features/turmas/components/TurmaGrid.tsx`
- Grid responsivo: 1 col mobile, 2 cols tablet, 3 cols desktop
- Recebe `turmas: TurmaResumo[]`
- Se lista vazia: ilustração + "Nenhuma turma cadastrada neste período"

### Página

`frontend/src/features/turmas/pages/TurmasPage.tsx`
- Rota: `/professor/turmas`
- `usePeriodosLetivos()` hook para carregar o seletor
- Dropdown de período letivo — ao trocar invalida query `['turmas', periodoId]`
- `<input>` de busca: filtra `turmas` localmente por `nome.toLowerCase().includes(filtro)`
- Estado de loading: renderiza 6 `TurmaCardSkeleton`
- Se busca sem resultado: "Nenhuma turma encontrada"
- Botão "Nova Turma": visível apenas se `getCurrentUser().perfil === 'admin'`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/professor/turmas` dentro do ProtectedRoute |

---

## Ordem de Implementação

```
1. types.ts — TurmaResumo, PeriodoLetivo
2. turmas.service.ts
3. useTurmas hook
4. TurmaCard
5. TurmaCardSkeleton
6. TurmaGrid
7. TurmasPage
8. Atualizar router
9. Testes: filtro client-side, badge visível apenas com pendências, botão admin
```

---

## Checklist de Validação

- [ ] Cards exibem todas as informações
- [ ] Badges apenas quando pendências > 0
- [ ] Troca de período invalida e recarrega dados
- [ ] Busca filtra em tempo real sem requisição
- [ ] Botão "Nova Turma" visível somente para admin
- [ ] Skeleton durante carregamento
- [ ] Estado vazio correto

---

## Resumo

- **8 arquivos** a criar (service, types, hook, 3 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova** (TanStack Query já instalado)
- **Complexidade mantida:** M
