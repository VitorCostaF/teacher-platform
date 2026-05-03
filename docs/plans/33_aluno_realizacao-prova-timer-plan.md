# Plano de Implementação — aluno_realizacao-prova-timer

> **Task origem:** `docs/Tasks/aluno_realizacao-prova-timer.md`
> **Escopo:** Frontend — Área do Aluno
> **Complexidade:** G
> **Sprint:** 4 — Área do Aluno
> **Depende de:** `aluno_realizacao-atividade-plan.md`, `backend-aluno_endpoint-entregas-plan.md`

---

## Contexto do Codebase

`QuestaoRenderer`, `QuestaoNavigator`, `AtividadeProgressBar`, `useAtividadePlayer`, `Button`, `ConfirmationModal` já existem. Componentes de questões reutilizados. Esta task adiciona timer baseado no servidor, autosave com sessão e detecção de saída de aba.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `QuestaoRenderer` | `src/features/aluno/components/QuestaoRenderer.tsx` | Mesmo despacho de tipo de questão |
| `QuestaoNavigator` | `src/features/aluno/components/QuestaoNavigator.tsx` | Mesmo navegador |
| Questões individuais | `src/features/aluno/components/questoes/` | Mesmos componentes de resposta |
| `ConfirmationModal` | `src/components/ui/ConfirmationModal.tsx` | Modal de aviso ao retornar de aba |

---

## Arquivos a Criar

### Serviço (extensão)

Adicionar ao `frontend/src/features/aluno/services/aluno.service.ts`:
```typescript
iniciarProva: (provaId: number) =>
  apiClient.post<SessaoProvaData>(`/provas/${provaId}/iniciar`).then(r => r.data),
autosaveProva: (provaId: number, sessaoId: number, payload: AutosavePayload) =>
  apiClient.put(`/provas/${provaId}/sessoes/${sessaoId}/autosave`, payload),
entregarProva: (provaId: number, sessaoId: number, respostas: RespostasMap) =>
  apiClient.post<EntregaResult>(`/provas/${provaId}/sessoes/${sessaoId}/entregar`, { respostas }).then(r => r.data),
```

### Tipos

```typescript
export interface SessaoProvaData {
  sessaoId: number; iniciadaEm: string; duracaoMinutos: number
  respostasParciais?: RespostasMap
}
export interface AutosavePayload {
  respostas: RespostasMap; eventoVisibilidade?: 'visible' | 'hidden'
}
```

### Componentes

`frontend/src/features/aluno/components/ProvaTimer.tsx`
- Recebe `iniciadaEm: string` (ISO8601 do servidor) e `duracaoMinutos: number`
- Calcula tempo restante: `duracaoMinutos * 60 - (Date.now() / 1000 - new Date(iniciadaEm).getTime() / 1000)`
- **Nunca usa `Date.now()` como base para o timer** — usa diferença desde `iniciadaEm` do servidor
- Atualiza via `setInterval(1000)` — apenas decrementa o delta
- Renderiza: `MM:SS`
- Quando < 5 minutos: classe `text-red-600 font-bold animate-pulse`; em mobile chama `navigator.vibrate([200, 100, 200])`
- Quando = 0: dispara callback `onExpire`

`frontend/src/features/aluno/components/AbaInativaModal.tsx`
- Modal não bloqueante exibido quando aluno retorna de outra aba
- "Você saiu desta janela às [hora]. Isso foi registrado."
- Botão "Entendi" para fechar

### Hook

`frontend/src/features/aluno/hooks/useProvaPlayer.ts`
- Extends a lógica de `useAtividadePlayer` mas para provas com sessão
- `iniciarSessao(provaId)` → chama `iniciarProva` → obtém `sessaoId` e `iniciadaEm`
- Autosave a cada 60s com `sessaoId`: `alunoService.autosaveProva(provaId, sessaoId, { respostas })`
- `visibilitychange` handler: ao esconder → `autosaveProva(..., { eventoVisibilidade: 'hidden' })`; ao mostrar → exibir `AbaInativaModal`
- `onTimerExpire()` → chama `entregarProva()` automaticamente com respostas atuais
- Backup offline: `useEffect` → ao ficar offline, salva `localStorage.setItem('prova_backup_${sessaoId}', JSON.stringify(respostas))`; ao ficar online, limpa backup e sincroniza
- Banner offline: estado `isOffline` baseado em `navigator.onLine` + eventos `online`/`offline`

### Página

`frontend/src/features/aluno/pages/RealizarProvaPage.tsx`
- Rota: `/aluno/provas/:id`
- Header com `<ProvaTimer>` centralizado
- Ao abrir: verifica se já existe sessão ativa (API retorna existente se sim)
- Ao reabrir com sessão ativa: timer continua do ponto correto (não reinicia)
- Banner offline (laranja) no topo quando `isOffline`
- Botão "Entregar" com modal de confirmação

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/aluno/provas/:id` |

---

## Ordem de Implementação

```
1. Tipos adicionais
2. Extensão de aluno.service.ts
3. ProvaTimer (cálculo baseado no servidor, teste com valor mockado de iniciadaEm)
4. AbaInativaModal
5. useProvaPlayer hook (visibilitychange, autosave com sessaoId, backup offline)
6. RealizarProvaPage
7. Atualizar router
8. Testes: timer calculado da data do servidor (não do cliente), entrega automática ao expirar, backup em localStorage offline
```

---

## Checklist de Validação

- [ ] Timer calculado com base no servidor (não Date.now())
- [ ] Timer vermelho + vibração em < 5 minutos
- [ ] Entrega automática quando = 0
- [ ] Saída de aba registra evento + modal ao retornar
- [ ] Banner offline + respostas em localStorage
- [ ] Ao reconectar: sync automático
- [ ] Sessão existente retomada com timer correto

---

## Resumo

- **5 arquivos** a criar (extensão service, ProvaTimer, AbaInativaModal, useProvaPlayer, RealizarProvaPage)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova** (reutiliza componentes de questão)
- **Complexidade mantida:** G
