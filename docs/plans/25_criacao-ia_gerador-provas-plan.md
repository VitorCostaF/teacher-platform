# Plano de Implementação — criacao-ia_gerador-provas

> **Task origem:** `docs/Tasks/criacao-ia_gerador-provas.md`
> **Escopo:** Frontend — Criação com IA
> **Complexidade:** G
> **Sprint:** 3 — Criação com IA
> **Depende de:** `backend-ia_endpoint-gerar-prova-plan.md`, `turmas_lista-e-cards-plan.md`

---

## Contexto do Codebase

`apiClient`, `Button`, `Input`, `Skeleton`, `useToast`, `useTurmas` (para dropdown de turma), TanStack Query e React Router já existem. Esta é a funcionalidade central de IA da plataforma.

---

## Arquivos a Criar

### Serviço

`frontend/src/features/criacao-ia/services/ia.service.ts`
```typescript
export const iaService = {
  gerarProva: (req: GerarProvaDto) =>
    apiClient.post<GeracaoResponse>('/ia/gerar-prova', req).then(r => r.data),
  regenerarQuestao: (req: RegerarQuestaoDto) =>
    apiClient.post<QuestaoGerada>('/ia/regenerar-questao', req).then(r => r.data),
  uploadConteudo: (file: File) => {
    const form = new FormData(); form.append('file', file)
    return apiClient.post<{ texto: string; aviso?: string }>('/upload/conteudo', form).then(r => r.data)
  },
}
```

### Tipos

`frontend/src/features/criacao-ia/types.ts`
```typescript
export interface QuestaoGerada {
  id: string; tipo: TipoQuestao; enunciado: string
  alternativas?: string[]; gabarito?: number
  dificuldade: string; topico: string; criteriosCorrecao?: string
}
export type TipoQuestao = 'multipla_escolha' | 'verdadeiro_falso' | 'dissertativa'
export interface GeracaoResponse { questoes: QuestaoGerada[]; tokensUsados: number }
```

### Hook principal

`frontend/src/features/criacao-ia/hooks/useGeradorProvas.ts`
- Estado: `config` (formulário esquerdo), `questoes: QuestaoGerada[]`, `isGenerating`, `conteudoTexto`
- `gerarComIA()` → valida config → chama `iaService.gerarProva()` → popula `questoes`
- `regenerarQuestao(id)` → chama `iaService.regenerarQuestao()` → substitui questão na lista
- `editarQuestao(id, changes)` → atualiza questão localmente (inline edit)
- `removerQuestao(id)` → filtra da lista; renumera
- `adicionarQuestaoManual()` → insere questão em branco para edição
- `uploadArquivo(file)` → chama `iaService.uploadConteudo()` → popula `conteudoTexto`; exibe aviso se insuficiente

### Componentes — Painel Esquerdo (Configuração)

`frontend/src/features/criacao-ia/components/ProvaConfigPanel.tsx`
- Select de turma (usa `useTurmas`)
- Input título, datepicker data aplicação
- Select dificuldade (Fácil/Médio/Difícil/Misto), input duração
- Checkboxes de tipo + número de questões por tipo
- Tabs para fonte: "Colar texto" | "Upload arquivo" | "Tópicos livres"
- Botão "Gerar com IA" (primário, largo) — disabled se config incompleta

`frontend/src/features/criacao-ia/components/ConteudoTabs.tsx`
- Tab "Colar texto": `<textarea>` de altura fixa
- Tab "Upload arquivo": drag-and-drop + input file (PDF/DOCX, 10MB)
  - Exibir barra de progresso do upload
  - Exibir aviso se conteúdo extraído < 100 palavras úteis
- Tab "Tópicos livres": lista de chips com input para adicionar

### Componentes — Painel Direito (Preview)

`frontend/src/features/criacao-ia/components/ProvaPreviewPanel.tsx`
- Estado vazio: ilustração + "Configure e clique em Gerar com IA"
- Estado gerando: animação de loading com mensagem "A IA está criando suas questões..."
- Estado com questões: lista numerada de `<QuestaoCard>`

`frontend/src/features/criacao-ia/components/QuestaoCard.tsx`
- Renderiza por tipo:
  - Múltipla escolha: enunciado + radio buttons desabilitados + gabarito marcado (professor)
  - V/F: toggle visual
  - Dissertativa: enunciado + campo "critérios de correção" editável
- Badge "Gerado com IA" verde no canto
- Ações: ✏️ "Editar" (modo inline) | 🔄 "Regenerar" | 🗑️ "Remover"
- Modo edição inline: campos de texto para enunciado e alternativas (sem recarregar o card)

### Barra de Ações

`frontend/src/features/criacao-ia/components/ProvaActionsBar.tsx`
- Botão "Adicionar questão manualmente"
- Botão "Regenerar tudo"
- Botão "Publicar" → navega para `/professor/criar/prova/:id/publicar` (salva rascunho primeiro)

### Página

`frontend/src/features/criacao-ia/pages/GeradorProvasPage.tsx`
- Rota: `/professor/criar/prova`
- Layout split: painel esquerdo (40%) + painel direito (60%)
- Mobile: painéis empilhados com botão de colapso

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/professor/criar/prova` |

---

## Ordem de Implementação

```
1. types.ts
2. ia.service.ts
3. useGeradorProvas hook
4. ConteudoTabs (upload + aviso de conteúdo insuficiente)
5. ProvaConfigPanel
6. QuestaoCard (múltipla escolha, VF, dissertativa)
7. ProvaPreviewPanel
8. ProvaActionsBar
9. GeradorProvasPage (layout split)
10. Atualizar router
11. Testes: upload de PDF, loading state, edição inline, regenerar questão
```

---

## Checklist de Validação

- [ ] Validação de config antes de chamar IA
- [ ] Loading animation durante geração
- [ ] Questões renderizadas por tipo
- [ ] Edição inline funciona
- [ ] Regenerar questão individual funciona
- [ ] Remoção atualiza numeração
- [ ] Upload vai para `/upload/conteudo` antes da IA
- [ ] Aviso de conteúdo insuficiente exibido

---

## Resumo

- **8 arquivos** a criar (service, types, hook, 5 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** G
