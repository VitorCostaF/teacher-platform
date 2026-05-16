# Backend Auth — Endpoints POST /auth/refresh e POST /auth/logout

> **Escopo:** backend-auth  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-auth_endpoint-post-login.md`

---

## Contexto

Dois endpoints complementares ao login: refresh para renovar o access token sem nova autenticação, e logout para invalidar a sessão no servidor.

---

## O que deve ser implementado

**POST /auth/refresh:**
- Ler refresh token do cookie httpOnly
- Validar assinatura e expiração do JWT
- Verificar se o refresh token existe e está ativo no banco (rotation — cada token é de uso único)
- Invalidar o refresh token atual no banco
- Gerar novo access token (1h) e novo refresh token (30d)
- Retornar novo access token no body e novo refresh token via `Set-Cookie`
- Retornar 401 se refresh token inválido, expirado ou não encontrado no banco

**POST /auth/logout:**
- Ler refresh token do cookie
- Invalidar o registro no banco (soft delete ou flag `revogado = true`)
- Limpar o cookie de refresh token (Set-Cookie com Max-Age=0)
- Retornar 204 sempre (idempotente — mesmo se o token não existir)

---

## Critérios de Aceite

**Refresh:**
- [ ] 200 retorna novo accessToken e seta novo cookie de refreshToken
- [ ] Refresh token antigo é invalidado após uso (não pode ser reutilizado)
- [ ] 401 para token inválido, expirado ou já utilizado
- [ ] Limite de 5 sessões simultâneas por usuário é verificado

**Logout:**
- [ ] 204 retornado sempre
- [ ] Refresh token é invalidado no banco
- [ ] Cookie de refresh token é removido no response (Max-Age=0)

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Endpoints de Autenticação > POST /auth/refresh e POST /auth/logout`

---

## Notas e Edge Cases

- Logout com cookie ausente deve retornar 204 mesmo assim (idempotente)
- Ao atingir o limite de 5 sessões no refresh, invalidar a sessão mais antiga

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes unitários para todos os cenários
- [ ] Sem erros no console / logs
- [ ] Code review realizado
