# Global — Tratamento de Sessão Expirada

> **Escopo:** global  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `login_redirecionamento-por-perfil.md`, `backend-auth_endpoint-refresh-logout.md`

---

## Contexto

Access tokens expiram em 1 hora. O sistema deve renovar o token silenciosamente sem interromper o usuário. Se a renovação falhar (refresh token expirado), deve redirecionar para login preservando a URL atual para retorno após reautenticação.

---

## O que deve ser implementado

- Implementar interceptor HTTP global que, ao receber 401, tenta `POST /auth/refresh` automaticamente
- Se refresh bem-sucedido: repetir a requisição original com novo access token (transparente para o usuário)
- Se refresh falhar (401 no refresh): salvar URL atual em `sessionStorage` → redirecionar para `/login` → exibir toast "Sua sessão expirou. Você foi redirecionado para o login."
- Implementar renovação proativa: 5 minutos antes de expirar, tentar renovar em background (baseado no `expiresIn` recebido no login)
- Durante prova em andamento: se renovação falhar, salvar respostas em localStorage e exibir modal: "Sua sessão expirou. Faça login novamente para continuar."
- Broadcast de logout entre abas: ao fazer logout em uma aba, as outras devem redirecionar para login

---

## Critérios de Aceite

- [ ] 401 em qualquer requisição dispara tentativa de refresh automaticamente
- [ ] Requisição original é repetida com sucesso após refresh bem-sucedido
- [ ] Falha no refresh redireciona para `/login` com URL salva em `sessionStorage`
- [ ] Toast de sessão expirada é exibido na tela de login
- [ ] URL salva é usada para redirecionamento após novo login
- [ ] Logout em uma aba propaga para outras abas abertas
- [ ] Durante prova: respostas são preservadas em localStorage se sessão expirar

---

## Especificação de Referência

- **Arquivo:** `07-comportamentos-globais.md`
- **Seção:** `1. Sessão e Autenticação`

---

## Notas e Edge Cases

- Evitar loop infinito: se o retry do refresh também retornar 401, não tentar renovar novamente
- Múltiplas requisições simultâneas com 401 devem aguardar um único refresh (fila de refresh)

---

## Definition of Done

- [ ] Interceptor implementado e funcionando
- [ ] Testes unitários para os cenários de refresh e falha
- [ ] Testado com token expirado de verdade (não apenas mockado)
- [ ] Code review realizado
