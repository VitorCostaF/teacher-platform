# Recuperação de Senha — Telas e Fluxo Completo

> **Escopo:** recuperar-senha  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `backend-auth_endpoint-recuperar-senha.md`

---

## Contexto

Usuário esqueceu sua senha e precisa redefini-la. O fluxo tem duas etapas: solicitar o link de recuperação e, depois de clicar no link do e-mail, definir a nova senha. Por segurança, o sistema não confirma se o e-mail existe.

---

## O que deve ser implementado

**Etapa 1 — `/recuperar-senha`:**
- Campo de e-mail e botão "Enviar instruções"
- Ao submeter: chamar `POST /auth/recuperar-senha`
- Independente do resultado (200 sempre), exibir: "Se este e-mail estiver cadastrado, você receberá as instruções em breve."
- Não exibir erro diferente para e-mails não cadastrados

**Etapa 2 — `/recuperar-senha/:token`:**
- Validar token ao montar (GET implícito — usar o POST diretamente na submissão)
- Campos: nova senha (com indicador de força) + confirmar senha
- Ao submeter com sucesso: redirecionar para `/login` com toast "Senha redefinida com sucesso!"
- Token expirado (410): exibir mensagem com link para voltar à etapa 1

---

## Critérios de Aceite

- [ ] Etapa 1 exibe sempre a mesma mensagem de sucesso independente do e-mail
- [ ] Etapa 1 não menciona se o e-mail existe ou não
- [ ] Etapa 2 valida os campos de senha (mesmas regras do primeiro acesso)
- [ ] Token expirado exibe mensagem com link para solicitar novo
- [ ] Após redefinição bem-sucedida, toast aparece na tela de login
- [ ] Botão de submit exibe loading durante a requisição

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Tela: Recuperação de Senha`

---

## Notas e Edge Cases

- O toast de sucesso deve aparecer na tela de `/login` após o redirect, não antes
- Link na etapa 2 deve ser single-use: ao recarregar após uso, mostrar "link já utilizado"

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Sem erros no console / logs
- [ ] Revisado por pelo menos um colega (code review)
- [ ] Testado em mobile e desktop
