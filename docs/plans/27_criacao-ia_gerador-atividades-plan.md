# Plano de Implementação — criacao-ia_gerador-atividades

> **Task origem:** `docs/Tasks/criacao-ia_gerador-atividades.md`
> **Escopo:** Frontend — Criação com IA
> **Complexidade:** M
> **Sprint:** 3 — Criação com IA
> **Depende de:** `criacao-ia_gerador-provas-plan.md`

---

## Contexto do Codebase

`ProvaConfigPanel`, `ProvaPreviewPanel`, `QuestaoCard`, `ProvaActionsBar`, `useGeradorProvas`, `ia.service.ts`, `types.ts` já existem do gerador de provas. Esta task **reutiliza extensivamente** esses componentes, adicionando apenas os campos específicos de atividade.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `ProvaPreviewPanel` | `src/features/criacao-ia/components/ProvaPreviewPanel.tsx` | Mesmo painel de preview e edição |
| `QuestaoCard` | `src/features/criacao-ia/components/QuestaoCard.tsx` | Mesmo card de questão (adicionar tipos novos) |
| `ProvaActionsBar` | `src/features/criacao-ia/components/ProvaActionsBar.tsx` | Mesmos botões de ação |
| `useGeradorProvas` | `src/features/criacao-ia/hooks/useGeradorProvas.ts` | Reutilizar lógica de geração e edição |
| `ConteudoTabs` | `src/features/criacao-ia/components/ConteudoTabs.tsx` | Mesmas tabs de fonte de conteúdo |
| `iaService` | `src/features/criacao-ia/services/ia.service.ts` | Mesmo endpoint de geração |

---

## Arquivos a Criar

### Componente de Configuração de Atividade

`frontend/src/features/criacao-ia/components/AtividadeConfigPanel.tsx`
- Estende `ProvaConfigPanel` ou cria variante específica de atividade
- Campos igual ao ProvaConfigPanel, **exceto**: sem campo duração obrigatória
- Campos **adicionais**:
  - **Tipo de entrega**: select (Online / PDF para imprimir / Ambos)
  - **Prazo de entrega**: datepicker com seleção de data + hora (`<input type="datetime-local">`)
- Tipos de questão adicionais nos checkboxes: "Leitura com perguntas", "Pesquisa com roteiro", "Projeto com etapas"

### Preview PDF A4

`frontend/src/features/criacao-ia/components/PreviewPDFA4.tsx`
- Renderizado quando tipo de entrega = "PDF para imprimir"
- Container com proporção A4 (210/297 mm em px), borda, margens simuladas
- Exibe cabeçalho, questões formatadas como impressão
- Botão "Abrir prévia em nova aba" → abre CSS print view

### Tipos adicionais

Adicionar ao `frontend/src/features/criacao-ia/types.ts`:
```typescript
export type TipoEntrega = 'ONLINE' | 'PDF' | 'AMBOS'
export type TipoQuestaoAtividade =
  | TipoQuestao
  | 'leitura_com_perguntas'
  | 'pesquisa_com_roteiro'
  | 'projeto_com_etapas'
```

### Página

`frontend/src/features/criacao-ia/pages/GeradorAtividadesPage.tsx`
- Rota: `/professor/criar/atividade`
- Layout igual ao `GeradorProvasPage` (split esquerdo/direito)
- Usa `AtividadeConfigPanel` em vez de `ProvaConfigPanel`
- Se `tipoEntrega === 'PDF'`: painel direito mostra `<PreviewPDFA4>` ao invés do preview padrão
- Reutiliza `useGeradorProvas` passando `tipo: 'atividade'`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/features/criacao-ia/types.ts` | Adicionar TipoEntrega, TipoQuestaoAtividade |
| `frontend/src/router/index.tsx` | Adicionar rota `/professor/criar/atividade` |

---

## Ordem de Implementação

```
1. Adicionar tipos em types.ts
2. AtividadeConfigPanel (baseado em ProvaConfigPanel)
3. PreviewPDFA4 (estilo A4 via CSS)
4. GeradorAtividadesPage
5. Atualizar router
6. Testes: campos de atividade, preview PDF, prazo com hora
```

---

## Checklist de Validação

- [ ] Campos específicos de atividade presentes e funcionando
- [ ] Tipo entrega "PDF" exibe preview A4
- [ ] Prazo aceita data + hora
- [ ] Tipos de atividade adicionais renderizados

---

## Resumo

- **3 arquivos** a criar (AtividadeConfigPanel, PreviewPDFA4, GeradorAtividadesPage)
- **2 arquivos** a modificar (types.ts, router)
- **Nenhuma dependência nova** (reutiliza componentes e hooks do gerador de provas)
- **Complexidade mantida:** M
