# Backend Auth — Endpoints de Convite e Recuperação de Senha

> **Escopo:** backend-auth  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-model_usuario.md`

---

## Contexto

Endpoints para ativar conta via convite (alunos/responsáveis) e para o fluxo de recuperação de senha. Ambos usam tokens de curta duração enviados por e-mail.

---

## O que deve ser implementado

**GET /auth/convite/:token:**
- Verificar se o token existe, não expirou (72h) e não foi usado
- 200: retornar `{ nome, email, valido: true }`
- 410 Gone: token expirado
- 409 Conflict: token já utilizado

**POST /auth/convite/:token/ativar:**
- Validar token novamente
- Validar body: `nome` (3-100 chars), `senha` (8+ chars, 1 número, 1 maiúscula), `confirmar_senha` (igual à senha)
- Atualizar nome e senha_hash do usuário
- Marcar token como utilizado
- Criar sessão e retornar tokens (igual ao login)

**POST /auth/recuperar-senha:**
- Aceitar `{ email }`
- Buscar usuário; se não encontrar, retornar 200 mesmo assim (não revelar existência)
- Se encontrar: gerar token de recuperação (1h), salvar no banco, enviar e-mail com link

**POST /auth/recuperar-senha/:token:**
- Validar token (1h de validade, não utilizado)
- Atualizar senha_hash
- Marcar token como utilizado e invalidar todas as sessões ativas do usuário
- 200 em sucesso, 410 se expirado

---

## Critérios de Aceite

- [ ] GET convite retorna 200/410/409 corretamente
- [ ] POST ativar valida todos os campos e retorna tokens após sucesso
- [ ] POST recuperar-senha retorna sempre 200 (mesmo e-mail inexistente)
- [ ] POST recuperar-senha/:token invalida sessões ativas após redefinição
- [ ] Tokens de convite e recuperação são de uso único

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seções:** `Tela: Primeiro Acesso` e `Tela: Recuperação de Senha`

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes unitários para todos os cenários de token
- [ ] Code review realizado
