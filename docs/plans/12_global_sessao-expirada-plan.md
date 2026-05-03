# Plano de Implementação — global_sessao-expirada

> **Task origem:** `docs/Tasks/global_sessao-expirada.md`
> **Escopo:** Frontend — Global
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `login_redirecionamento-por-perfil-plan.md`, `backend-auth_endpoint-refresh-logout-plan.md`

---

## Contexto do Codebase

`apiClient` com response interceptor já existe (`global_tratamento-erros-servidor`). `authStore` com `clearAuth()`, `setAuth()`, `getAccessToken()` disponível. `authService` com endpoint `/auth/login` disponível. Esta task adiciona refresh automático de token ao interceptor e broadcast de logout entre abas.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `apiClient` | `src/lib/api.ts` | Adicionar interceptor de 401 com refresh |
| `authStore` | `src/store/authStore.ts` | `setAuth`, `clearAuth`, `getAccessToken`, `getCurrentUser` |
| `toastEmitter` | `src/lib/toastEmitter.ts` | Toast de sessão expirada |
| `ProtectedRoute` | `src/components/auth/ProtectedRoute.tsx` | Salvar URL atual antes de redirecionar |

---

## Arquivos a Criar

### Serviço de Refresh

`frontend/src/features/auth/services/auth.service.ts` (adicionar ao existente)
```typescript
// Adicionar ao authService existente:
refresh: () =>
  apiClient.post<{ accessToken: string; expiresIn: number }>('/auth/refresh').then(r => r.data),
logout: () =>
  apiClient.post('/auth/logout').then(r => r.data),
```

### Auth Interceptor

`frontend/src/lib/authInterceptor.ts`
- Lida com 401 no response interceptor (separado do error interceptor geral)
- Lógica de fila: múltiplas requisições simultâneas com 401 aguardam um único refresh
  ```typescript
  let isRefreshing = false
  let failedQueue: Array<{ resolve: Function; reject: Function }> = []
  ```
- Se refresh bem-sucedido: `setAuth(newToken, currentUser)` → repetir todas as requisições na fila
- Se refresh falhar (401 no refresh): `clearAuth()` → `sessionStorage.setItem('redirect_url', currentPath)` → navegar para `/login` → `toastEmitter.emit('warning', 'Sua sessão expirou...')`
- Registrar como interceptor separado via `apiClient.interceptors.response.use()`

### Renovação Proativa

`frontend/src/features/auth/hooks/useProactiveRefresh.ts`
```typescript
// useEffect que agenda refresh 5 minutos antes de expirar
// Baseado em expiresIn recebido no login (salvo no authStore)
// Usa setTimeout; cancela e reagenda ao fazer novo login
```

Atualizar `authStore` para também armazenar `expiresAt: Date | null`.

### Broadcast de Logout

`frontend/src/lib/broadcastAuth.ts`
```typescript
const channel = new BroadcastChannel('auth')

export function broadcastLogout() {
  channel.postMessage({ type: 'LOGOUT' })
}

export function listenForLogout(callback: () => void) {
  channel.onmessage = (e) => {
    if (e.data.type === 'LOGOUT') callback()
  }
}
```

`frontend/src/hooks/useAuthBroadcast.ts`
- `useEffect` que registra `listenForLogout` e, ao receber sinal, chama `clearAuth()` e navega para `/login`
- Adicionar em `App.tsx` ou layout raiz

### Preservação de Prova

`frontend/src/features/aluno/hooks/useProvaOfflineBackup.ts`
- Monitorar `authInterceptor` para evento de sessão expirada durante prova
- Salvar respostas em `localStorage` com chave `prova_backup_${sessaoId}`
- Exibir `ConfirmationModal` com mensagem "Sua sessão expirou. Faça login para continuar."
- Após novo login, detectar backup e oferecer restauração

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/lib/api.ts` | Registrar `authInterceptor` de 401 (importar authInterceptor.ts) |
| `frontend/src/store/authStore.ts` | Adicionar campo `expiresAt` + `setAuth` atualizado |
| `frontend/src/features/auth/hooks/useLogin.ts` | Ao fazer login, iniciar `useProactiveRefresh` com `expiresIn` recebido |

---

## Ordem de Implementação

```
1. Atualizar auth.service.ts — refresh e logout
2. Atualizar authStore — expiresAt
3. authInterceptor.ts — fila de refresh, redirect ao expirar
4. Atualizar api.ts — registrar authInterceptor
5. broadcastAuth.ts + useAuthBroadcast hook
6. useProactiveRefresh hook
7. Atualizar useLogin — iniciar proactiveRefresh
8. useProvaOfflineBackup (pode ser stub por ora)
9. Testes: fila de refresh (múltiplos 401 simultâneos), falha de refresh → redirect
10. Teste: broadcast entre abas (abrir duas, fazer logout em uma)
```

---

## Checklist de Validação

- [ ] 401 dispara refresh automático
- [ ] Requisição original repetida com sucesso após refresh
- [ ] Falha no refresh → redirect `/login` com URL em sessionStorage
- [ ] Toast de sessão expirada exibido
- [ ] URL salva usada após novo login
- [ ] Logout em uma aba propaga para outras
- [ ] Prova: respostas em localStorage se sessão expirar

---

## Resumo

- **5 arquivos** a criar (authInterceptor, broadcastAuth, useAuthBroadcast, useProactiveRefresh, useProvaOfflineBackup)
- **3 arquivos** a modificar (api.ts, authStore, useLogin)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
