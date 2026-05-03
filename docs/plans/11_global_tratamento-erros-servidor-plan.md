# Plano de Implementação — global_tratamento-erros-servidor

> **Task origem:** `docs/Tasks/global_tratamento-erros-servidor.md`
> **Escopo:** Frontend — Global
> **Complexidade:** M
> **Sprint:** 0 — Fundação
> **Depende de:** `login_validacao-e-estados-de-erro-plan.md` (Toast, ErrorBanner existem)

---

## Contexto do Codebase

`apiClient` em `src/lib/api.ts` já existe com `timeout: 15000`. `Toast` e `ToastProvider` já existem em `src/components/feedback/`. Esta task implementa o interceptor HTTP global que centraliza o tratamento de erros para toda a aplicação.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `useToast` / `ToastProvider` | `src/components/feedback/Toast.tsx` | Exibir erros globais como toast |
| `apiClient` | `src/lib/api.ts` | Adicionar interceptors |

---

## Arquivos a Criar

### Mapeamento de Erros

`frontend/src/lib/errors.ts`
```typescript
export const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: 'Dados inválidos. Verifique os campos e tente novamente.',
  401: 'Sessão expirada. Faça login novamente.',
  403: 'Você não tem permissão para esta ação.',
  404: 'Recurso não encontrado.',
  409: 'Conflito: este registro já existe.',
  422: 'Dados inválidos no servidor.',
  429: 'Muitas tentativas. Aguarde um momento.',
  500: 'Erro interno do servidor. Tente novamente.',
  502: 'Serviço temporariamente indisponível.',
  503: 'Servidor em manutenção. Tente em alguns minutos.',
}

export const TIMEOUT_MESSAGE = 'Não foi possível conectar. Verifique sua internet e tente novamente.'

export class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public fieldErrors?: Record<string, string>
  ) { super(message) }
}
```

### Loading Bar Global

`frontend/src/components/feedback/LoadingBar.tsx`
- Barra fina no topo da página (similar ao NProgress)
- Estado global via Context ou Zustand simples
- `useLoadingBar()` hook: `{ start, done }`
- Animação CSS: de 0% a ~80% ao iniciar, salta para 100% ao completar

`frontend/src/components/feedback/LoadingBarProvider.tsx`
- Context provider; `LoadingBar` renderizado dentro dele
- Envolver em `main.tsx`

### Skeleton Components (base)

`frontend/src/components/feedback/Skeleton.tsx`
```tsx
// Bloco animado com shimmer para placeholders de carregamento
interface SkeletonProps {
  className?: string  // controlar dimensões via Tailwind
}
```

### Interceptor Global

Atualizar `frontend/src/lib/api.ts` com response interceptor completo:
```typescript
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
      // Toast de timeout/rede — acessar via toast fora do React (event emitter)
      toastEmitter.emit('error', TIMEOUT_MESSAGE)
      return Promise.reject(new ApiError(0, TIMEOUT_MESSAGE))
    }

    const status = error.response?.status
    if (status === 401) {
      // Delegar ao authInterceptor (task global_sessao-expirada) via evento
      // Não tratar aqui para evitar loop
      return Promise.reject(error)
    }
    if (status === 400 || status === 422) {
      // Erros de campo: retornar para o componente tratar inline
      return Promise.reject(new ApiError(status, error.response.data?.message, error.response.data?.errors))
    }

    // Demais erros: toast global
    const message = HTTP_ERROR_MESSAGES[status] ?? HTTP_ERROR_MESSAGES[500]
    toastEmitter.emit('error', message)
    return Promise.reject(new ApiError(status, message))
  }
)
```

`frontend/src/lib/toastEmitter.ts`
```typescript
// EventEmitter simples para chamar toast de fora do React
type Listener = (message: string) => void
const listeners: Record<string, Listener[]> = {}
export const toastEmitter = {
  on: (event: string, fn: Listener) => { ... },
  emit: (event: string, msg: string) => { ... },
  off: (event: string, fn: Listener) => { ... },
}
```

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/lib/api.ts` | Adicionar response interceptor completo com mapeamento de erros e toastEmitter |
| `frontend/src/components/feedback/ToastProvider.tsx` | Escutar eventos do `toastEmitter` e enfileirar toasts |
| `frontend/src/main.tsx` | Adicionar `<LoadingBarProvider>` |
| `frontend/src/router/index.tsx` | Adicionar `loader` de rota para start/done do LoadingBar |

---

## Ordem de Implementação

```
1. errors.ts — constantes e classe ApiError
2. toastEmitter.ts — event emitter leve
3. Atualizar ToastProvider — escutar toastEmitter
4. LoadingBar + LoadingBarProvider
5. Skeleton component base
6. Atualizar api.ts — response interceptor
7. Atualizar main.tsx — LoadingBarProvider
8. Testes: interceptor trata 403, 500, timeout corretamente
9. Testes: toast fila com múltiplos erros simultâneos
```

---

## Checklist de Validação

- [ ] Toast 403/404/500/503 no canto superior direito
- [ ] Toast tem botão X
- [ ] Múltiplos toasts em fila (não sobrepostos)
- [ ] 400/422 com `errors` de campo: **não** dispara toast (retorna para componente)
- [ ] Timeout em 15s dispara toast de rede
- [ ] Nenhuma mensagem técnica exibida ao usuário
- [ ] Loading bar durante navegação de páginas

---

## Resumo

- **5 arquivos** a criar (errors.ts, toastEmitter, LoadingBar, LoadingBarProvider, Skeleton)
- **4 arquivos** a modificar (api.ts, ToastProvider, main.tsx, router)
- **Nenhuma dependência nova** (usa axios e React Context)
- **Complexidade mantida:** M
