# Plano de Implementação — criacao-ia_revisao-publicacao

> **Task origem:** `docs/Tasks/criacao-ia_revisao-publicacao.md`
> **Escopo:** Frontend — Criação com IA
> **Complexidade:** M
> **Sprint:** 3 — Criação com IA
> **Depende de:** `criacao-ia_gerador-provas-plan.md`, `backend-avaliacoes_endpoint-publicar-plan.md`

---

## Contexto do Codebase

`QuestaoCard` (modo somente leitura), `Button`, `Input`, `useToast`, `useTurmas`, `apiClient`, TanStack Query já existem. Esta task é a etapa final do ciclo de criação.

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/criacao-ia/services/ia.service.ts`:
```typescript
salvarRascunho: (data: SalvarRascunhoDto) =>
  apiClient.post<{ id: number }>('/provas/rascunho', data).then(r => r.data),
publicar: (provaId: number, config: PublicarDto) =>
  apiClient.post(`/provas/${provaId}/publicar`, config).then(r => r.data),
getPreview: (provaId: number) =>
  apiClient.get(`/provas/${provaId}/preview`).then(r => r.data),
```

### Tipos

Adicionar ao `frontend/src/features/criacao-ia/types.ts`:
```typescript
export interface PublicarDto {
  disponivelEm: string
  encerraEm?: string
  turmasIds: number[]
  embaralharQuestoes: boolean
  embaralharAlternativas: boolean
  liberarGabaritoApos: 'entrega' | 'encerramento' | 'manual'
  peso: number
}
```

### Componentes

`frontend/src/features/criacao-ia/components/PublicacaoConfigForm.tsx`
- DateTimePicker para "disponível em" (pode ser futuro → cria como "Agendada")
- DateTimePicker para "encerra em" (opcional)
- MultiSelect de turmas (usa `useTurmas`) — checklist ou select múltiplo
- Toggles: embaralhar questões, embaralhar alternativas
- Select liberação de gabarito: "Após entrega" | "Após encerramento" | "Manual"
- Input numérico: peso na nota final (0.0 a 10.0)

`frontend/src/features/criacao-ia/components/AvaliacaoPreviewAluno.tsx`
- Renderiza a prova exatamente como o aluno veria
- Questões embaralhadas (não reflete gabarito)
- Cabeçalho com título, disciplina, duração, data
- Usa `QuestaoCard` em modo "aluno" (sem gabarito marcado)
- Botão "Ver como aluno" / "Ver como professor" toggle

### Página

`frontend/src/features/criacao-ia/pages/RevisaoPublicacaoPage.tsx`
- Rota: `/professor/criar/prova/:id/publicar` (e `/professor/criar/atividade/:id/publicar`)
- Recebe `id` via `useParams` — é o ID do rascunho salvo
- Carrega preview via `iaService.getPreview(id)`
- Layout duas colunas: preview (esquerda, 55%) + formulário de configuração (direita, 45%)
- Botão "Publicar" (primário):
  1. Chama `iaService.publicar(id, config)`
  2. Loading no botão
  3. Sucesso: `useToast().success("Prova publicada!")` + redirect para `/professor/turmas`
- Botão "Salvar como Rascunho" (secundário):
  - Chama `iaService.salvarRascunho(data)` sem publicar
  - Toast de sucesso + permanecer na página

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Rotas `/professor/criar/prova/:id/publicar` e `/professor/criar/atividade/:id/publicar` |

---

## Ordem de Implementação

```
1. Adicionar tipos em types.ts e métodos em ia.service.ts
2. AvaliacaoPreviewAluno
3. PublicacaoConfigForm (multiselect de turmas, toggles, datepickers)
4. RevisaoPublicacaoPage
5. Atualizar router
6. Testes: publicação com data futura → status "Agendada"; múltiplas turmas selecionadas
```

---

## Checklist de Validação

- [ ] Preview renderiza como aluno veria (sem gabarito)
- [ ] Todas as configurações de publicação funcionam
- [ ] Multiselect de turmas funciona
- [ ] Data futura cria prova "Agendada"
- [ ] Após publicar: toast + redirect
- [ ] Rascunho salvo pode ser editado depois

---

## Resumo

- **4 arquivos** a criar (extensão service, tipos, 2 componentes, 1 página)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
