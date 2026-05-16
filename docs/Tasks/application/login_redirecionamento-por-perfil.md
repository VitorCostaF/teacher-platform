# Tela de Login — Integração com API e Redirecionamento por Perfil

> **Escopo:** login  
> **Tipo:** Frontend  
> **Complexidade estimada:** M  
> **Depende de:** `login_validacao-e-estados-de-erro.md`, `backend-auth_endpoint-post-login.md`

---

## Contexto

Após validação bem-sucedida das credenciais pelo backend, o frontend deve armazenar os tokens de forma segura e redirecionar o usuário para o dashboard correto conforme seu perfil.

---

## O que deve ser implementado

- Chamar `POST /auth/login` com `{ email, senha }` ao submeter o formulário
- Armazenar `accessToken` em memória (não em localStorage)
- Armazenar `refreshToken` em httpOnly cookie (configurado pelo backend via `Set-Cookie`)
- Após login bem-sucedido, redirecionar conforme `perfil` retornado:
  - `professor` → `/professor/dashboard`
  - `aluno` → `/aluno/feed`
  - `responsavel` → `/responsavel/acompanhamento`
  - `coordenador` / `admin` → `/admin/visao-geral`
- Se houver URL salva em `sessionStorage` (redirecionamento por sessão expirada), redirecionar para ela após login

---

## Critérios de Aceite

- [ ] POST /auth/login é chamado com os dados corretos ao submeter
- [ ] `accessToken` é armazenado em memória (não em localStorage ou sessionStorage)
- [ ] Após login, professor é redirecionado para `/professor/dashboard`
- [ ] Após login, aluno é redirecionado para `/aluno/feed`
- [ ] Após login, responsável é redirecionado para `/responsavel/acompanhamento`
- [ ] Após login, coordenador/admin é redirecionado para `/admin/visao-geral`
- [ ] URL salva em `sessionStorage` tem prioridade no redirecionamento

---

## Especificação de Referência

- **Arquivo:** `01-autenticacao.md`
- **Seção:** `Fluxo: Login > Caminho Principal`

---

## Detalhes Técnicos

**Chamadas de API:**
| Método | Endpoint | Quando | Dados enviados |
|--------|----------|--------|---------------|
| POST | /auth/login | Ao submeter formulário válido | `{ email, senha }` |

**Perfis e rotas:**

| perfil | Rota de destino |
|--------|----------------|
| professor | /professor/dashboard |
| aluno | /aluno/feed |
| responsavel | /responsavel/acompanhamento |
| coordenador | /admin/visao-geral |
| admin | /admin/visao-geral |

---

## Notas e Edge Cases

- Perfil desconhecido: redirecionar para `/` e logar warning no console
- O `refreshToken` vem como cookie httpOnly — o frontend não acessa seu valor diretamente

---

## Definition of Done

- [ ] Código implementado e funcionando conforme critérios de aceite
- [ ] Testes de integração cobrindo todos os perfis
- [ ] Sem erros no console / logs
- [ ] Revisado por pelo menos um colega (code review)
- [ ] Testado em Chrome, Firefox e Safari
