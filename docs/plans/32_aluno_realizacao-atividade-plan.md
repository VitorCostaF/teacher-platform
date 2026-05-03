# Plano de Implementação — aluno_realizacao-atividade

> **Task origem:** `docs/Tasks/aluno_realizacao-atividade.md`
> **Escopo:** Frontend — Área do Aluno
> **Complexidade:** G
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `backend-aluno_endpoint-entregas-plan.md`

---

## Contexto do Codebase

`alunoService`, `apiClient`, `Button`, `ConfirmationModal`, `useToast` disponíveis. Esta task implementa o player de atividade com navegação entre questões, autosave e suporte a upload de arquivo.

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/aluno/services/aluno.service.ts`:
```typescript
getAtividade: (id: number) =>
  apiClient.get<AtividadeDetalhe>(`/atividades/${id}`).then(r => r.data),
salvarRascunho: (id: number, respostas: RespostasMap) =>
  apiClient.put(`/atividades/${id}/rascunho`, { respostas }),
entregar: (id: number, respostas: RespostasMap) =>
  apiClient.post<EntregaResult>(`/atividades/${id}/entregar`, { respostas }).then(r => r.data),
uploadRespostaArquivo: (file: File) => {
  const form = new FormData(); form.append('file', file)
  return apiClient.post<{ url: string }>('/upload/resposta', form).then(r => r.data)
},
```

### Tipos adicionais

```typescript
export type RespostasMap = Record<number, string | number | string[]>
export interface AtividadeDetalhe {
  id: number; titulo: string; disciplina: string; prazo: string
  permiteAtraso: boolean; questoes: QuestaoAtividade[]
  respostasRascunho?: RespostasMap
}
export interface QuestaoAtividade {
  id: number; numero: number; tipo: string; enunciado: string
  alternativas?: string[]
}
```

### Componentes

`frontend/src/features/aluno/components/questoes/QuestaoMultiplaEscolha.tsx`
- Radio buttons para alternativas
- Props: `questao`, `resposta: number | null`, `onChange`

`frontend/src/features/aluno/components/questoes/QuestaoVerdadeiroFalso.tsx`
- Toggle dois botões: Verdadeiro / Falso

`frontend/src/features/aluno/components/questoes/QuestaoDissertativa.tsx`
- `<textarea>` com contador de caracteres
- Props: `resposta: string`, `onChange`

`frontend/src/features/aluno/components/questoes/QuestaoUploadArquivo.tsx`
- Input file + preview do arquivo selecionado
- Aceita PDF/imagem, máx 5MB
- Progresso do upload
- Exibir arquivo já enviado se rascunho existente

`frontend/src/features/aluno/components/QuestaoRenderer.tsx`
- Componente de despacho: renderiza o componente correto baseado em `questao.tipo`

`frontend/src/features/aluno/components/QuestaoNavigator.tsx`
- Índice de questões: lista numérica clicável
- Indicador visual: respondida (verde), não respondida (cinza), atual (azul)
- Botões Anterior / Próxima

`frontend/src/features/aluno/components/AtividadeProgressBar.tsx`
- "Questão X de Y" + barra de progresso proporcional

### Hook

`frontend/src/features/aluno/hooks/useAtividadePlayer.ts`
- Estado: `questaoAtual`, `respostas: RespostasMap`, `isSubmitting`, `isAutoSaving`
- Inicializa `respostas` a partir do rascunho existente
- `setResposta(questaoId, valor)` → atualiza mapa
- `isTodaRespondida()` → verifica se todas têm resposta
- Autosave: `useEffect` com `setInterval(30000)` → chama `alunoService.salvarRascunho()` silenciosamente; em caso de falha de rede, tenta na próxima janela sem alertar
- `entregar()` → chama `alunoService.entregar()` → navega para resultado

### Página

`frontend/src/features/aluno/pages/RealizarAtividadePage.tsx`
- Rota: `/aluno/atividades/:id`
- Verifica `item.status`; se ENTREGUE: redireciona para resultado
- Se prazo vencido + `!permiteAtraso`: botão "Entregar" desabilitado + mensagem
- Se prazo vencido + `permiteAtraso`: aviso amarelo + marca entrega como atrasada
- Botão "Salvar Rascunho" (manual)
- Botão "Entregar" (`disabled` se `!isTodaRespondida`) → `ConfirmationModal` antes de confirmar

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/aluno/atividades/:id` |

---

## Ordem de Implementação

```
1. Tipos adicionais
2. Extensão de aluno.service.ts
3. Questoes: MultiplaEscolha, VF, Dissertativa, Upload
4. QuestaoRenderer (despacho por tipo)
5. QuestaoNavigator
6. AtividadeProgressBar
7. useAtividadePlayer hook (autosave, estado das respostas)
8. RealizarAtividadePage
9. Atualizar router
10. Testes: autosave a cada 30s, botão entregar com pendentes, rascunho pré-carregado, upload arquivo
```

---

## Checklist de Validação

- [ ] Navegação livre entre questões
- [ ] Respostas preservadas ao navegar
- [ ] Autosave silencioso a cada 30s
- [ ] Botão "Entregar" desabilitado com pendentes
- [ ] Modal de confirmação antes de entregar
- [ ] Prazo vencido sem permissão de atraso: botão desabilitado
- [ ] Upload de arquivo (5MB max)
- [ ] Rascunho pré-carregado ao retornar

---

## Resumo

- **10 arquivos** a criar (extensão service, 4 questões, renderer, navigator, progress, hook, página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** G
