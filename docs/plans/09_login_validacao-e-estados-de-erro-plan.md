# Plano de Implementação — login_validacao-e-estados-de-erro

> **Task origem:** `docs/Tasks/login_validacao-e-estados-de-erro.md`
> **Escopo:** Frontend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `login_estrutura-do-formulario-plan.md` (LoginForm, Button, Input existem)

---

## Contexto do Codebase

`LoginForm.tsx`, `Button.tsx`, `Input.tsx`, `PasswordInput.tsx` já existem em `frontend/src/features/auth/` e `frontend/src/components/ui/`. `react-hook-form`, `zod` e `@hookform/resolvers` já instalados. Esta task adiciona validação, estados de loading/erro e o componente Toast.

---

## Componentes Existentes para Reutilizar

| Componente / Hook | Caminho | Por que reutilizar |
|-------------------|---------|-------------------|
| `Button` | `src/components/ui/Button.tsx` | Já tem prop `loading`, variante primary |
| `Input` | `src/components/ui/Input.tsx` | Já exibe erro inline via prop `error` |
| `PasswordInput` | `src/features/auth/components/PasswordInput.tsx` | Já implementado |

## Bibliotecas Disponíveis

| Biblioteca | Uso nesta task |
|-----------|---------------|
| `react-hook-form` | Controle do formulário, validação onBlur, submit |
| `zod` | Schema de validação |
| `@hookform/resolvers/zod` | Integrar zod com react-hook-form |

---

## Arquivos a Criar

### Schema de Validação

`frontend/src/features/auth/schemas/login.schema.ts`
```typescript
import { z } from 'zod'

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'E-mail é obrigatório')
    .email('Formato de e-mail inválido'),
  senha: z.string().min(1, 'Senha é obrigatória'),
})

export type LoginFormData = z.infer<typeof loginSchema>
```

### Toast Component

`frontend/src/components/feedback/Toast.tsx`
- Toast no canto superior direito, duração 5s, botão X manual
- Fila: múltiplos toasts empilhados verticalmente
- Variantes: `success` (verde), `error` (vermelho), `warning` (amarelo)
- `useToast()` hook exportado para uso em qualquer componente

`frontend/src/components/feedback/ToastProvider.tsx`
- Context provider que mantém fila de toasts
- Renderiza `<Toast>` items sobre a página
- Envolver em `main.tsx`

### Banner de Erro

`frontend/src/components/feedback/ErrorBanner.tsx`
```tsx
interface ErrorBannerProps {
  message: string
  variant?: 'error' | 'warning'
  onDismiss?: () => void
}
```
- Banner com cor por variant
- Botão X para fechar
- Usado para erros 401 (vermelho) e 423 (amarelo)

### Hook de Login (só lógica, sem API call nesta task)

`frontend/src/features/auth/hooks/useLoginForm.ts`
- Usa `useForm<LoginFormData>` com `zodResolver(loginSchema)` e `mode: 'onBlur'`
- Retorna `{ register, handleSubmit, formState: { errors, isSubmitting } }`
- Gerencia estado de erro da API: `errorMessage: string | null`
- Gerencia estado de bloqueio 423: `isLocked: boolean`, `unlockTime: Date | null`
- `onSubmit(data)` — nesta task apenas loga no console; a chamada à API vem no próximo plano

---

## Arquivos a Modificar

`frontend/src/features/auth/components/LoginForm.tsx`
- Integrar `useLoginForm()` substituindo o formulário estático
- Campos `<Input>` recebem `{...register('email')}` e `error={errors.email?.message}`
- `<PasswordInput>` recebe `{...register('senha')}`
- Estado "Submetendo": `<Button loading={isSubmitting} disabled={isSubmitting}>`
- Estado "Erro credencial" (401): renderizar `<ErrorBanner variant="error">` acima do botão
- Estado "Conta bloqueada" (423): `<ErrorBanner variant="warning">` com horário calculado
- Campos `disabled` quando `isSubmitting || isLocked`

`frontend/src/main.tsx`
- Envolver app com `<ToastProvider>`

---

## Arquivos de Referência

| Arquivo | Por que consultar |
|---------|------------------|
| `frontend/src/components/ui/Button.tsx` | Props disponíveis (loading, disabled, variant) |
| `frontend/src/components/ui/Input.tsx` | Como o prop `error` é renderizado |

---

## Ordem de Implementação

```
1. login.schema.ts — schema zod
2. ToastProvider + Toast component
3. ErrorBanner component
4. useLoginForm hook
5. Modificar LoginForm para integrar hook e estados visuais
6. Modificar main.tsx — adicionar ToastProvider
7. Testes: renderizar LoginForm em cada estado (padrão, loading, 401, 423, 500)
```

---

## Checklist de Validação

- [ ] E-mail exibe erro inline ao perder foco com valor inválido
- [ ] E-mail exibe erro ao submeter vazio
- [ ] Durante requisição: botão com spinner, campos `disabled`
- [ ] 401: banner vermelho sem limpar campos
- [ ] 423: banner amarelo com horário de desbloqueio em pt-BR
- [ ] 500/timeout: toast no canto superior direito
- [ ] Após erro: formulário reabilitado (não trava)

---

## Resumo

- **5 arquivos** a criar (schema, Toast, ToastProvider, ErrorBanner, useLoginForm)
- **2 arquivos** a modificar (LoginForm, main.tsx)
- **Bibliotecas aproveitadas:** react-hook-form, zod, @hookform/resolvers
- **Complexidade mantida:** M
