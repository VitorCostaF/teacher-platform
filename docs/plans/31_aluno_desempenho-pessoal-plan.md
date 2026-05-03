# Plano de Implementação — aluno_desempenho-pessoal

> **Task origem:** `docs/Tasks/aluno_desempenho-pessoal.md`
> **Escopo:** Frontend — Área do Aluno
> **Complexidade:** M
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `backend-aluno_endpoint-feed-plan.md`

---

## Contexto do Codebase

`alunoService`, `recharts` (já instalado), `Skeleton`, `BottomNavigation`, TanStack Query disponíveis. Esta task é a tela "Meu Desempenho" do aluno com gráficos e gamificação.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `recharts LineChart` | instalado | Gráfico de evolução de notas |
| `BottomNavigation` | `src/features/aluno/components/BottomNavigation.tsx` | Navegação inferior padrão do aluno |

---

## Arquivos a Criar

### Tipos (adicionais)

Adicionar ao `frontend/src/features/aluno/types.ts`:
```typescript
export interface DesempenhoData {
  mediaGlobal: number; totalEntregues: number; percentualFrequencia: number
  porDisciplina: DisciplinaDesempenho[]
  evolucaoNotas: Array<{ avaliacaoNome: string; nota: number; disciplina: string }>
  conquistas: Conquista[]
}
export interface DisciplinaDesempenho {
  id: number; nome: string; media: number; tendencia: 'UP' | 'DOWN' | 'STABLE'
  proximaAvaliacao?: string
}
export interface Conquista { tipo: string; descricao: string; icone: string; obtidaEm: string }
```

### Componentes

`frontend/src/features/aluno/components/ResumoCardsAluno.tsx`
- 3 cards: "Média Geral: X.X", "Atividades Entregues: X", "Frequência: X%"
- Layout: linha horizontal em desktop, grid 3 colunas em mobile

`frontend/src/features/aluno/components/DisciplinaCard.tsx`
- Props: `disciplina: DisciplinaDesempenho`
- Média + seta de tendência (↑ verde, ↓ vermelho, → cinza)
- Próxima avaliação se existir
- Clicável (não tem rota específica por ora — link futuro)

`frontend/src/features/aluno/components/EvolucaoNotasAlunoChart.tsx`
- `LineChart` Recharts
- Select de disciplina acima do gráfico
- Filtra `evolucaoNotas` pela disciplina selecionada
- Tooltip com nome da avaliação e nota

`frontend/src/features/aluno/components/ConquistaBadge.tsx`
- Badge circular com ícone e nome
- Tooltip/popover no hover ou tap: nome + critério de obtenção + data
- `aria-label` para acessibilidade

`frontend/src/features/aluno/components/ConquistasSection.tsx`
- Grid de `<ConquistaBadge>`
- Se nenhuma conquista: "Continue estudando para ganhar suas primeiras conquistas!"

### Página

`frontend/src/features/aluno/pages/DesempenhoPage.tsx`
- Rota: `/aluno/desempenho`
- `useQuery(['aluno', 'desempenho'])` com `alunoService.getDesempenho()`
- Skeleton das seções durante loading
- Seções em ordem: Resumo → Cards por disciplina → Gráfico evolução → Conquistas
- Atalho "Ver flashcards dos tópicos fracos" → navega para `/aluno/flashcards` com tópico com menor acerto

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Confirmar/adicionar rota `/aluno/desempenho` |

---

## Ordem de Implementação

```
1. Tipos adicionais
2. ResumoCardsAluno
3. DisciplinaCard
4. EvolucaoNotasAlunoChart (LineChart Recharts)
5. ConquistaBadge (tooltip acessível)
6. ConquistasSection
7. DesempenhoPage
8. Atualizar router
9. Testes: gráfico muda ao trocar disciplina, tooltip de conquista, atalho flashcards
```

---

## Checklist de Validação

- [ ] Média global calculada e exibida
- [ ] Tendência por disciplina correta
- [ ] Gráfico muda ao trocar disciplina
- [ ] Badges com critério visível no hover/tap
- [ ] Link para flashcards no tópico correto

---

## Resumo

- **6 arquivos** a criar (tipos, 4 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova** (recharts já instalado)
- **Complexidade mantida:** M
