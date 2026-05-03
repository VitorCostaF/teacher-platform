# Backend Auth — Endpoint POST /auth/login

> **Escopo:** backend-auth  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_usuario.md`, `backend-model_sessao.md`

---

## Contexto

Endpoint central de autenticação. Valida credenciais, aplica rate limit e bloqueio por tentativas, e retorna tokens JWT para o frontend.

---

## O que deve ser implementado

- Rota `POST /auth/login` pública (sem autenticação)
- Validar body: `email` (formato e-mail, obrigatório) e `senha` (não vazia, obrigatória)
- Buscar usuário pelo e-mail; comparar senha com bcrypt
- Verificar se a conta está ativa (`ativo = true`); retornar 403 se não
- Verificar se está bloqueada por tentativas — retornar 423 com `desbloqueiaEm`
- Em caso de credenciais erradas: incrementar contador de tentativas; após 5 em 15min, bloquear por 15min
- Em caso de sucesso: gerar `accessToken` (JWT, 1h) e `refreshToken` (JWT, 30d)
- Armazenar refresh token no banco (tabela `sessoes`) associado ao usuário e user-agent
- Retornar `Set-Cookie` com refresh token httpOnly + Secure + SameSite=Strict
- Aplicar rate limit: 10 req/min por IP

---

## Critérios de Aceite

- [ ] 200 retornado com accessToken, refreshToken (cookie), expiresIn, perfil e dados do usuário
- [ ] 401 retornado para credenciais inválidas (mesmo formato independente de qual campo está errado)
- [ ] 403 retornado para conta inativa
- [ ] 423 retornado após 5 tentativas com campo `desbloqueiaEm` correto
- [ ] Contador de tentativas é resetado após login bem-sucedido
- [ ] Rate limit de 10 req/min por IP retorna 429
- [ ] Refresh token é armazenado no banco e vinculado ao usuário
- [ ] Cookie de refresh token é httpOnly, Secure e SameSite=Strict

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Endpoints de Autenticação > POST /auth/login` e `Regras de Negócio — Autenticação`

---

## Detalhes Técnicos

**Response 200:**
```json
{
  "accessToken": "eyJ...",
  "expiresIn": 3600,
  "perfil": "professor",
  "usuario": { "id": "uuid", "nome": "...", "email": "...", "avatarUrl": "..." }
}
```

**Response 423:**
```json
{ "error": "ACCOUNT_LOCKED", "message": "Conta bloqueada temporariamente.", "desbloqueiaEm": "ISO8601" }
```

---

## Notas e Edge Cases

- O bloqueio deve ser implementado com Redis (TTL de 15min) para não poluir o banco principal
- Nunca retornar qual campo está errado — sempre mensagem genérica no 401
- O user-agent deve ser armazenado junto à sessão para exibição futura (ex: "Chrome no Windows")

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes unitários para todos os casos de resposta
- [ ] Testes de integração com banco real
- [ ] Sem erros no console / logs
- [ ] Code review realizado
