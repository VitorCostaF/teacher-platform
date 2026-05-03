# Plano de Implementação — recuperar-senha_fluxo-completo

> **Task origem:** `docs/Tasks/recuperar-senha_fluxo-completo.md`
> **Escopo:** Frontend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `backend-auth_endpoint-convite-plan.md`

---

## Contexto do Codebase

`Input`, `Button`, `PasswordInput`, `PasswordStrengthIndicator`, `convite.schema.ts` (validações de senha), `useToast`, `apiClient` e `authService` já existem. React Router configurado. O fluxo tem duas etapas em rotas separadas.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `PasswordInput` | `src/features/auth/components/PasswordInput.tsx` | Campo de senha com toggle |
| `PasswordStrengthIndicator` | `src/features/auth/components/PasswordStrengthIndicator.tsx` | Indicador de força |
| `Button` | `src/components/ui/Button.tsx` | Botões com loading |
| `Input` | `src/components/ui/Input.tsx` | Campo de e-mail |
| `useToast` | `src/components/feedback/Toast.tsx` | Toast de sucesso após redefinição |
| `convite.schema.ts` | Reaproveitar validações de senha (min 8, número, maiúscula) |

---

## Arquivos a Criar

### Serviço

Adicionar ao `frontend/src/features/auth/services/auth.service.ts`:
```typescript
solicitarRecuperacaoSenha: (email: string) =>
  apiClient.post('/auth/recuperar-senha', { email }).then(r => r.data),

redefinirSenha: (token: string, data: { senha: string }) =>
  apiClient.post(`/auth/recuperar-senha/${token}`, data).then(r => r.data),
```

### Schemas

`frontend/src/features/auth/schemas/recuperar-senha.schema.ts`
```typescript
export const recuperarSenhaStep1Schema = z.object({
  email: z.string().min(1, 'E-mail é obrigatório').email('Formato de e-mail inválido'),
})

export const recuperarSenhaStep2Schema = z.object({
  senha: z
    .string()
    .min(8, 'Mínimo de 8 caracteres')
    .regex(/[0-9]/, 'Deve conter pelo menos 1 número')
    .regex(/[A-Z]/, 'Deve conter pelo menos 1 letra maiúscula'),
  confirmarSenha: z.string(),
}).refine(d => d.senha === d.confirmarSenha, {
  message: 'As senhas não coincidem',
  path: ['confirmarSenha'],
})
```

### Páginas

`frontend/src/features/auth/pages/RecuperarSenhaPage.tsx` (Etapa 1 — `/recuperar-senha`)
- Formulário com campo de e-mail + botão "Enviar instruções"
- Ao submeter: chama `authService.solicitarRecuperacaoSenha(email)`
- **Independente do resultado (200 ou erro 404):** exibir mensagem genérica "Se este e-mail estiver cadastrado, você receberá as instruções em breve."
- Não diferencia e-mail cadastrado de não cadastrado (privacidade)
- Link "Voltar ao login" → `/login`

`frontend/src/features/auth/pages/RedefinirSenhaPage.tsx` (Etapa 2 — `/recuperar-senha/:token`)
- Lê `:token` via `useParams`
- Campos: nova senha + confirmar senha (com `PasswordStrengthIndicator` na nova senha)
- Ao submeter: chama `authService.redefinirSenha(token, { senha })`
- Sucesso: `useToast().success("Senha redefinida com sucesso!")` → navegar para `/login`
  - Importante: toast deve aparecer **na tela de login** após redirect; usar `navigate('/login', { state: { toastMessage: '...' } })`
- Erro 410 (token expirado): exibir mensagem "Este link expirou." com link para `/recuperar-senha`
- Erro genérico de já utilizado: "Este link já foi utilizado. Acesse pelo login." com link `/login`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rotas `/recuperar-senha` e `/recuperar-senha/:token` (fora do ProtectedRoute) |
| `frontend/src/features/auth/pages/LoginPage.tsx` | Ler `location.state?.toastMessage` e exibir toast se presente |

---

## Ordem de Implementação

```
1. recuperar-senha.schema.ts
2. Adicionar ao authService: solicitarRecuperacaoSenha, redefinirSenha
3. RecuperarSenhaPage (etapa 1 — simples, mensagem genérica)
4. RedefinirSenhaPage (etapa 2 — campos de senha + estados de erro)
5. Atualizar router — duas novas rotas
6. Atualizar LoginPage — ler location.state para toast de sucesso
7. Testes: etapa 1 sempre mostra mensagem genérica; etapa 2 token expirado vs sucesso
```

---

## Checklist de Validação

- [ ] Etapa 1 sempre exibe mensagem genérica (independente do e-mail existir)
- [ ] Etapa 2 valida senha com mesmas regras do convite
- [ ] Token expirado (410) exibe mensagem com link para etapa 1
- [ ] Após redefinição: toast no `/login` (não antes do redirect)
- [ ] Loading durante submit em ambas as etapas

---

## Resumo

- **3 arquivos** a criar (schema, 2 páginas)
- **2 arquivos** a modificar (router, LoginPage)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
