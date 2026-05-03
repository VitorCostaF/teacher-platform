# Plano de Implementação — aluno_flashcards

> **Task origem:** `docs/Tasks/aluno_flashcards.md`
> **Escopo:** Frontend — Área do Aluno
> **Complexidade:** M
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `backend-aluno_endpoint-feed-plan.md` (endpoint de flashcards)

---

## Contexto do Codebase

`alunoService`, `Button`, `Skeleton`, TanStack Query, `BottomNavigation` disponíveis. Esta task implementa a tela de flashcards com animação de virada e integração com o algoritmo SM-2 do backend.

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/aluno/services/aluno.service.ts`:
```typescript
getFlashcards: (disciplinaId?: number) =>
  apiClient.get<FlashcardData[]>('/aluno/flashcards', { params: { disciplina: disciplinaId } }).then(r => r.data),
avaliarFlashcard: (cardId: number, sabia: boolean) =>
  apiClient.post(`/aluno/flashcards/${cardId}/avaliacao`, { sabia }),
```

### Tipos

```typescript
export interface FlashcardData { id: number; pergunta: string; resposta: string; topico: string }
```

### Componentes

`frontend/src/features/aluno/components/Flashcard.tsx`
- Container com perspectiva 3D CSS (`perspective: 1000px`)
- Frente: pergunta
- Verso: resposta
- Estado `isFlipped: boolean`; ao clicar/tocar → inverte
- Animação: `rotateY(180deg)` em `transition: transform 0.6s`
- Acessibilidade: `role="button"`, `aria-label="Clique para revelar resposta"`

`frontend/src/features/aluno/components/FlashcardActions.tsx`
- Visível apenas após virar o card (`isFlipped`)
- Dois botões: "Sabia ✓" (verde) e "Não sabia ✗" (vermelho)
- Ao clicar: registra avaliação + exibe próximo card

`frontend/src/features/aluno/components/FlashcardProgressBar.tsx`
- "X de Y cards revisados" + barra proporcional
- Atualiza conforme cards respondidos

### Hook

`frontend/src/features/aluno/hooks/useFlashcards.ts`
- Estado: `cards: FlashcardData[]`, `indiceAtual`, `respondidos`, `sessaoEncerrada`
- `responder(sabia)` → chama `alunoService.avaliarFlashcard(cards[atual].id, sabia)` → avança para próximo
- `encerrarSessao()` → navega de volta (progresso já salvo no servidor pelo endpoint)
- Se todos respondidos: `sessaoEncerrada = true` → exibir tela de parabéns

### Página

`frontend/src/features/aluno/pages/FlashcardsPage.tsx`
- Rota: `/aluno/flashcards`
- Select de disciplina + select de tópico (opcional)
- Skeleton durante carregamento
- Se nenhum card disponível: "Nenhum flashcard disponível para este tópico"
- `<Flashcard>` centralizado
- `<FlashcardProgressBar>` no topo
- `<FlashcardActions>` abaixo do card (ocultos até virar)
- Botão "Encerrar sessão" (secundário)

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/aluno/flashcards` |

---

## Ordem de Implementação

```
1. Tipos adicionais
2. Extensão de aluno.service.ts
3. Flashcard (animação CSS 3D — testar em mobile)
4. FlashcardActions
5. FlashcardProgressBar
6. useFlashcards hook
7. FlashcardsPage
8. Atualizar router
9. Testes: animação em touch, "não sabia" registrado, estado vazio, encerrar sessão
```

---

## Checklist de Validação

- [ ] Animação de virada funciona em touch e click
- [ ] "Sabia" / "Não sabia" registram avaliação
- [ ] Progresso da sessão salvo
- [ ] Estado vazio exibido
- [ ] Sessão pode ser encerrada a qualquer momento

---

## Resumo

- **5 arquivos** a criar (extensão service, Flashcard, FlashcardActions, FlashcardProgressBar, hook, página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova** (animação apenas com CSS/Tailwind)
- **Complexidade mantida:** M
