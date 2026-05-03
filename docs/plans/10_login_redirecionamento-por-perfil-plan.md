# Plano de Implementação — login_redirecionamento-por-perfil

> **Task origem:** `docs/Tasks/login_redirecionamento-por-perfil.md`
> **Escopo:** Frontend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `login_validacao-e-estados-de-erro-plan.md`, `backend-auth_endpoint-post-login-plan.md`

---

## Contexto do Codebase

`LoginForm.tsx`, `useLoginForm.ts`, `ErrorBanner`, `Toast`, `apiClient` já existem. `react-hook-form` com schema zod já integrado. Esta task conecta o formulário à API real, armazena o token em memória e implementa redirecionamento por perfil.

---

## Componentes / Utilitários Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `apiClient` | `src/lib/api.ts` | Cliente axios configurado com `withCredentials: true` |
| `useLoginForm` | `src/features/auth/hooks/useLoginForm.ts` | Lógica de form e estados de erro |
| `useToast` | `src/components/feedback/Toast.tsx` | Notificação de erro de servidor |

---

## Arquivos a Criar

### Serviço de Auth

`frontend/src/features/auth/services/auth.service.ts`
```typescript
import { apiClient } from '@/lib/api'

export interface LoginDto { email: string; senha: string }
export interface AuthUser { id: string; nome: string; email: string; perfil: Perfil; avatarUrl: string }
export interface LoginResponse { accessToken: string; expiresIn: number; perfil: Perfil; usuario: AuthUser }

export type Perfil = 'professor' | 'aluno' | 'responsavel' | 'coordenador' | 'admin'

export const authService = {
  login: (data: LoginDto) =>
    apiClient.post<LoginResponse>('/auth/login', data).then(r => r.data),
}
```

### Store de Autenticação

`frontend/src/store/authStore.ts`
- Módulo de estado em memória (variável de módulo, **não** localStorage)
- `let accessToken: string | null = null`
- `let currentUser: AuthUser | null = null`
- Funções exportadas: `setAuth(token, user)`, `clearAuth()`, `getAccessToken()`, `getCurrentUser()`, `isAuthenticated()`
- **Nota:** não usar localStorage para tokens (requisito de segurança da spec)

### Interceptors HTTP

`frontend/src/lib/api.ts` (modificar existente)
- Request interceptor: injeta `Authorization: Bearer ${getAccessToken()}` se token disponível
- Response interceptor: trata erros globalmente (implementação completa na task `global_tratamento-erros-servidor`)

### Hook de Login (atualizar)

`frontend/src/features/auth/hooks/useLogin.ts` (novo hook — substitui useLoginForm para lógica de API)
```typescript
// Retorno do hook
return {
  login,           // função chamada pelo onSubmit
  isLoading,       // boolean
  errorMessage,    // string | null — 401 genérico
  isLocked,        // boolean — 423
  unlockTime,      // Date | null — horário de desbloqueio
}
```
- Chama `authService.login()`
- Sucesso: `setAuth(token, user)` → verificar `sessionStorage.getItem('redirect_url')` → se existir, redirecionar para ele; senão, redirecionar por perfil
- Erro 401: `errorMessage = "E-mail ou senha incorretos."`
- Erro 423: `isLocked = true`, `unlockTime = new Date(error.response.data.desbloqueiaEm)`
- Erro 500/timeout: `useToast().error("Não foi possível conectar...")`

### Mapa de Rotas por Perfil

`frontend/src/features/auth/utils/profileRoutes.ts`
```typescript
export const ROTAS_POR_PERFIL: Record<Perfil, string> = {
  professor: '/professor/dashboard',
  aluno: '/aluno/feed',
  responsavel: '/responsavel/acompanhamento',
  coordenador: '/admin/visao-geral',
  admin: '/admin/visao-geral',
}
```

### Proteção de Rotas

`frontend/src/components/auth/ProtectedRoute.tsx`
```tsx
// Redireciona para /login se não autenticado
// Salva URL atual em sessionStorage antes de redirecionar
export function ProtectedRoute({ children }: { children: React.ReactNode })
```

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/features/auth/components/LoginForm.tsx` | Trocar `useLoginForm` por `useLogin` (que faz a chamada API real) |
| `frontend/src/router/index.tsx` | Envolver rotas autenticadas com `<ProtectedRoute>`; adicionar redirect de `/` para `/login` |
| `frontend/src/lib/api.ts` | Adicionar request interceptor com token |

---

## Ordem de Implementação

```
1. auth.service.ts — interface e chamada à API
2. store/authStore.ts — armazenamento em memória
3. Atualizar api.ts — request interceptor
4. profileRoutes.ts — mapa de rotas
5. useLogin.ts — hook com API call e redirecionamento
6. ProtectedRoute.tsx
7. Atualizar LoginForm.tsx — trocar hook
8. Atualizar router/index.tsx — ProtectedRoute
9. Testes de integração: login de cada perfil → redirect correto
10. Teste: URL salva em sessionStorage tem prioridade
```

---

## Checklist de Validação

- [ ] POST /auth/login chamado com dados corretos
- [ ] accessToken em memória (não localStorage)
- [ ] professor → /professor/dashboard
- [ ] aluno → /aluno/feed
- [ ] responsavel → /responsavel/acompanhamento
- [ ] coordenador/admin → /admin/visao-geral
- [ ] URL do sessionStorage tem prioridade
- [ ] Perfil desconhecido → `/` + console.warn

---

## Resumo

- **5 arquivos** a criar (service, store, hook, profileRoutes, ProtectedRoute)
- **3 arquivos** a modificar (LoginForm, router, api.ts)
- **Bibliotecas aproveitadas:** axios (apiClient), react-router-dom
- **Complexidade mantida:** M
