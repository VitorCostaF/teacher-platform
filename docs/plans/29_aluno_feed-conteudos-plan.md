# Plano de Implementação — aluno_feed-conteudos

> **Task origem:** `docs/Tasks/aluno_feed-conteudos.md`
> **Escopo:** Frontend — Área do Aluno
> **Complexidade:** M
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `backend-aluno_endpoint-feed-plan.md`

---

## Contexto do Codebase

`apiClient`, `authStore`, `Skeleton`, `Button`, TanStack Query disponíveis. Esta task é a tela inicial do aluno com design mobile-first e navegação inferior.

---

## Arquivos a Criar

### Serviço

`frontend/src/features/aluno/services/aluno.service.ts`
```typescript
export const alunoService = {
  getFeed: () =>
    apiClient.get<FeedData>('/aluno/feed').then(r => r.data),
  getDesempenho: () =>
    apiClient.get<DesempenhoData>('/aluno/desempenho').then(r => r.data),
}
```

### Tipos

`frontend/src/features/aluno/types.ts`
```typescript
export interface FeedData {
  urgentes: FeedItem[]; paraFazer: FeedItem[]
  novosConteudos: ConteudoItem[]; recomendados: RecomendacaoItem[]
}
export interface FeedItem {
  id: number; tipo: 'PROVA' | 'ATIVIDADE'; titulo: string
  disciplina: string; prazo: string; status: string; atrasado: boolean
}
export interface ConteudoItem { id: number; titulo: string; tipo: 'texto' | 'video' | 'pdf'; tempoLeituraMinutos: number }
export interface RecomendacaoItem { id: number; titulo: string; explicacao: string; tipo: string }
```

### Componentes

`frontend/src/features/aluno/components/FeedItemCard.tsx`
- Props: `item: FeedItem`
- Cor de fundo: urgente → amarelo/vermelho; normal → branco
- Renderiza: tipo (badge), título, disciplina, prazo formatado, status (chip)
- Clicável: navega para `/aluno/atividades/:id` ou `/aluno/provas/:id`

`frontend/src/features/aluno/components/ConteudoCard.tsx`
- Ícone por tipo: 📄 texto, 🎥 vídeo, 📋 PDF
- Título, tempo de leitura
- Clicável

`frontend/src/features/aluno/components/RecomendacaoCard.tsx`
- Badge "Sugerido para você" (azul)
- Título + texto explicativo contextual ("Você teve dificuldade em X")

`frontend/src/features/aluno/components/FeedSection.tsx`
- Props: `title: string`, `items: React.ReactNode[]`, `emptyMessage?: string`
- Apenas renderizado se `items.length > 0` (exceto "Para fazer" que mostra vazio)

`frontend/src/features/aluno/components/AlunoHeader.tsx`
- Saudação com nome do aluno
- Badge de notificações (número)
- Pontos de gamificação (ícone + número)

`frontend/src/features/aluno/components/BottomNavigation.tsx`
- Barra fixa no `bottom-0` com 4 ícones: Feed | Atividades | Desempenho | Perfil
- Ativo: ícone colorido; inativo: cinza
- Usa `NavLink` do React Router para highlight automático
- Somente mobile (hidden em desktop)

### Página

`frontend/src/features/aluno/pages/FeedPage.tsx`
- Rota: `/aluno/feed`
- `useQuery` para carregar feed
- Skeleton: 3 cards placeholder durante loading
- Seções em ordem: Urgente (se existir) → Para Fazer → Novos Conteúdos → Recomendados
- `<BottomNavigation>` no rodapé

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rotas `/aluno/feed`, `/aluno/atividades`, `/aluno/desempenho` |

---

## Ordem de Implementação

```
1. types.ts
2. aluno.service.ts
3. BottomNavigation (base para todas as telas do aluno)
4. AlunoHeader
5. FeedItemCard
6. ConteudoCard
7. RecomendacaoCard
8. FeedSection
9. FeedPage
10. Atualizar router
11. Testes: seção Urgente ausente quando sem itens urgentes, badge correto
```

---

## Checklist de Validação

- [ ] Seção "Urgente" aparece apenas com prazo < 24h
- [ ] Cards exibem prazo, tipo, disciplina, status
- [ ] Badge de notificação correto
- [ ] Recomendações com texto explicativo
- [ ] Navegação inferior fixa no mobile
- [ ] Layout funciona em 375px

---

## Resumo

- **9 arquivos** a criar (service, types, 5 componentes, 1 página + BottomNavigation)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
