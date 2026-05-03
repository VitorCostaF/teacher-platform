# Plano de Implementação — primeiro-acesso_tela-convite

> **Task origem:** `docs/Tasks/primeiro-acesso_tela-convite.md`
> **Escopo:** Frontend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `backend-auth_endpoint-convite-plan.md`

---

## Contexto do Codebase

`authService`, `apiClient`, `authStore`, `PasswordInput`, `Button`, `Input`, `LoginForm` (para padrão de estilo), `Skeleton`, `ErrorBanner` e `useToast` já existem. `ProtectedRoute` e `profileRoutes` existem para redirecionamento pós-ativação. React Router já configurado.

---

## Componentes Existentes para Reutilizar

| Item | Caminho | Por que reutilizar |
|------|---------|-------------------|
| `PasswordInput` | `src/features/auth/components/PasswordInput.tsx` | Campo de senha com toggle |
| `Button` | `src/components/ui/Button.tsx` | Botão de submit com loading |
| `Input` | `src/components/ui/Input.tsx` | Campos de nome, senha e confirmação |
| `Skeleton` | `src/components/feedback/Skeleton.tsx` | Loading enquanto valida token |
| `ErrorBanner` | `src/components/feedback/ErrorBanner.tsx` | Estado de erro de rede |
| `authStore.setAuth` | `src/store/authStore.ts` | Armazenar tokens após ativação |
| `ROTAS_POR_PERFIL` | `src/features/auth/utils/profileRoutes.ts` | Redirecionar após ativação |

---

## Arquivos a Criar

### Serviço

Adicionar ao `frontend/src/features/auth/services/auth.service.ts`:
```typescript
validarTokenConvite: (token: string) =>
  apiClient.get<ConviteTokenResponse>(`/auth/convite/${token}`).then(r => r.data),

ativarConta: (token: string, data: AtivarContaDto) =>
  apiClient.post<LoginResponse>(`/auth/convite/${token}/ativar`, data).then(r => r.data),
```

Tipos:
```typescript
interface ConviteTokenResponse {
  nome: string
  email: string
  perfil: Perfil
  status: 'VALIDO' | 'EXPIRADO' | 'JA_USADO'
}

interface AtivarContaDto {
  nome: string
  senha: string
}
```

### Schema de Validação

`frontend/src/features/auth/schemas/convite.schema.ts`
```typescript
export const conviteSchema = z.object({
  nome: z.string().min(3, 'Nome deve ter entre 3 e 100 caracteres').max(100),
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

### Componente de Força de Senha

`frontend/src/features/auth/components/PasswordStrengthIndicator.tsx`
- Recebe a senha como prop
- Calcula pontuação: 0-2 = Fraca, 3-4 = Média, 5 = Forte
- Critérios: ≥8 chars, número, maiúscula, minúscula, caractere especial
- Renderiza barra colorida (vermelho → amarelo → verde) + texto
- Atualiza em tempo real no `onChange` da senha

### Página

`frontend/src/features/auth/pages/ConvitePage.tsx`
- Lê `:token` via `useParams`
- `useEffect` → chama `authService.validarTokenConvite(token)` ao montar
- Estado de loading: `<Skeleton>` ocupando espaço do formulário
- Estado VALIDO: renderiza formulário com nome pré-preenchido (editável)
- Estado EXPIRADO: mensagem "Este link expirou. Solicite um novo à sua escola." sem formulário
- Estado JA_USADO: mensagem + link `/login`
- Estado de erro de rede: `<ErrorBanner>` com botão "Tentar novamente"
- Ao submeter com sucesso: `setAuth(token, user)` → redirecionar por perfil

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/src/router/index.tsx` | Adicionar rota `/convite/:token` (fora do ProtectedRoute) |

---

## Ordem de Implementação

```
1. convite.schema.ts
2. Adicionar validarTokenConvite e ativarConta ao authService
3. PasswordStrengthIndicator
4. ConvitePage — estados de loading, expirado, já usado, erro de rede
5. ConvitePage — formulário com react-hook-form + schema + submissão
6. Atualizar router — rota /convite/:token
7. Testes: 3 estados do token (válido, expirado, já usado)
8. Teste: indicador de força em tempo real
```

---

## Checklist de Validação

- [ ] Token válido: nome pré-preenchido da API
- [ ] Token expirado: mensagem correta sem formulário
- [ ] Token já usado: mensagem correta com link /login
- [ ] Indicador de força atualizado em tempo real
- [ ] Confirmar senha valida igualdade ao perder foco
- [ ] Após ativação: redirecionar por perfil
- [ ] Skeleton durante validação do token

---

## Resumo

- **4 arquivos** a criar (schema, PasswordStrengthIndicator, ConvitePage + serviço atualizado)
- **1 arquivo** a modificar (router)
- **Nenhuma dependência nova**
- **Complexidade mantida:** M
