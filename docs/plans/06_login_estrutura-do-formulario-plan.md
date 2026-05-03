# Plano de Implementação — login_estrutura-do-formulario

> **Task origem:** `docs/Tasks/login_estrutura-do-formulario.md`
> **Escopo:** Frontend — Autenticação
> **Complexidade:** P
> **Sprint:** 1 — Autenticação
> **Depende de:** Nenhuma (primeiro task de frontend — inclui inicialização do projeto)

---

## Contexto do Codebase

**Não existe frontend ainda.** Esta task inicializa o projeto frontend e implementa a estrutura visual da tela de login. O backend Spring Boot fica em `./` (raiz); o frontend ficará em `./frontend/`.

---

## Inicialização do Projeto Frontend

```bash
# Na raiz do repositório
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install

# Dependências principais
npm install react-router-dom @tanstack/react-query axios react-hook-form zod @hookform/resolvers
npm install -D tailwindcss @tailwindcss/vite
```

### Configuração de paths em `frontend/vite.config.ts`
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
```

### `frontend/src/lib/api.ts`
```typescript
import axios from 'axios'

export const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 15000,
})
```

### `frontend/src/router/index.tsx`
```tsx
import { createBrowserRouter } from 'react-router-dom'
import { LoginPage } from '@/features/auth/pages/LoginPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
])
```

### `frontend/src/main.tsx`
```tsx
import { RouterProvider } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { router } from '@/router'

const queryClient = new QueryClient()

ReactDOM.createRoot(document.getElementById('root')!).render(
  <QueryClientProvider client={queryClient}>
    <RouterProvider router={router} />
  </QueryClientProvider>
)
```

---

## Arquivos a Criar (após inicialização)

### Feature: Auth

`frontend/src/features/auth/pages/LoginPage.tsx`
- Página que centraliza o layout
- Em desktop (≥768px): card centralizado com `max-w-md`
- Em mobile (<768px): fullscreen, sem card, padding lateral

`frontend/src/features/auth/components/LoginForm.tsx`
- Logo da plataforma no topo com `alt="Teacher Platform"`
- Input de e-mail: `type="email"`, `autoComplete="email"`, `autoFocus`
- Input de senha: `type="password"` com botão de olho (toggle visibilidade)
- Botão "Entrar": `type="submit"`, `className="w-full"`, variante primária
- Link "Esqueci minha senha" → navega para `/recuperar-senha`
- **Apenas estado visual padrão** nesta task — sem lógica de validação ou API

`frontend/src/features/auth/components/PasswordInput.tsx`
- Input `type` controlado por estado local (`password` ↔ `text`)
- Ícone de olho aberto/fechado (usar SVG inline ou Lucide Icons)
- Aceita todas as props de `<input>` via spread

### Componentes UI base

`frontend/src/components/ui/Button.tsx`
```tsx
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'destructive'
  loading?: boolean
}
```
- Tailwind classes por variant
- Se `loading=true`: exibe spinner SVG + desabilita

`frontend/src/components/ui/Input.tsx`
```tsx
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: string
}
```
- Exibe mensagem de erro abaixo do input se `error` presente
- Borda vermelha quando `error` definido

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar redirect de `/` para `/login` (rota raiz) |

---

## Ordem de Implementação

```
1. Inicializar projeto Vite + instalar dependências
2. Configurar vite.config.ts (paths, proxy, tailwind)
3. Criar src/lib/api.ts
4. Criar componentes UI base: Button, Input
5. Criar PasswordInput
6. Criar LoginForm (visual apenas)
7. Criar LoginPage
8. Configurar router/index.tsx com rota /login
9. Atualizar main.tsx
10. Verificar layout em 375px e 1280px
```

---

## Checklist de Validação

- [ ] Rota `/login` acessível sem autenticação
- [ ] Campo e-mail com `type=email` e `autoComplete=email`
- [ ] Campo senha com toggle de visibilidade funcional
- [ ] Botão "Entrar" ocupa largura total
- [ ] Link "Esqueci minha senha" navega para `/recuperar-senha`
- [ ] Layout responsivo: card desktop, fullscreen mobile
- [ ] `autoFocus` no campo e-mail ao carregar

---

## Resumo

- **9 arquivos** a criar (2 páginas/componentes auth, 2 UI base, configs do projeto)
- **1 arquivo** a modificar (router)
- **Projeto frontend inicializado** com Vite + React 18 + TypeScript + Tailwind + React Router + React Query + axios
- **Complexidade mantida:** P
