# Spec 01 — Autenticação

> **Macro funcionalidade:** Autenticação e controle de acesso  
> **Perfis envolvidos:** Todos  
> **Plataforma:** Web e Mobile

---

## Telas desta Spec

1. [Login](#tela-login)
2. [Cadastro de Aluno / Responsável](#tela-cadastro)
3. [Recuperação de Senha](#tela-recuperação-de-senha)
4. [Primeiro Acesso (convite)](#tela-primeiro-acesso)

---

## Fluxo: Login

> **Objetivo:** Autenticar um usuário existente e redirecioná-lo para a área correta conforme seu perfil.  
> **Pré-condição:** Usuário possui cadastro ativo na plataforma.  
> **Pós-condição:** Usuário autenticado com JWT válido, redirecionado para o dashboard do seu perfil.

### Caminho Principal

1. Usuário acessa `/login`
2. Preenche e-mail e senha
3. Clica em "Entrar"
   - Sistema valida campos no frontend
   - Chama `POST /auth/login`
   - Exibe spinner no botão durante a requisição
4. Backend valida credenciais e retorna JWT + refresh token
5. Frontend armazena tokens (httpOnly cookie ou memory, nunca localStorage)
6. Redirecionamento conforme perfil:
   - Professor → `/professor/dashboard`
   - Aluno → `/aluno/feed`
   - Responsável → `/responsavel/acompanhamento`
   - Coordenador → `/admin/visao-geral`

### Caminhos de Erro

**Credenciais inválidas:**
- Exibe mensagem genérica: "E-mail ou senha incorretos." (nunca especificar qual campo está errado)
- Não limpa os campos
- Após 5 tentativas consecutivas: bloqueia conta por 15 minutos e exibe aviso

**Conta inativa:**
- Mensagem: "Sua conta está desativada. Entre em contato com a escola."
- Não oferece link para recuperação de senha

**Servidor indisponível:**
- Toast: "Não foi possível conectar. Verifique sua internet e tente novamente."

```mermaid
flowchart TD
    A[Acessa /login] --> B[Preenche e-mail e senha]
    B --> C[Clica em Entrar]
    C --> D{Frontend válido?}
    D -- Não --> E[Exibe erros inline]
    E --> B
    D -- Sim --> F[POST /auth/login]
    F --> G{Resposta do servidor}
    G -- 200 OK --> H[Armazena tokens]
    H --> I{Perfil do usuário}
    I -- Professor --> J[/professor/dashboard]
    I -- Aluno --> K[/aluno/feed]
    I -- Responsável --> L[/responsavel/acompanhamento]
    G -- 401 --> M[Exibe erro genérico]
    M --> B
    G -- 423 Bloqueado --> N[Exibe aviso de bloqueio temporário]
    G -- 500 --> O[Toast de erro de servidor]
```

---

## Tela: Login

> **Rota:** `/login`  
> **Autenticação:** Não requerida (redireciona para dashboard se já autenticado)  
> **Perfis com acesso:** Todos

### Layout e Componentes

- **Logo da plataforma** — centralizado no topo
- **Campo E-mail** — input type=email, autocomplete=email
- **Campo Senha** — input type=password com botão de toggle de visibilidade
- **Botão "Entrar"** — primário, largura total, estado de loading com spinner
- **Link "Esqueci minha senha"** — abaixo do botão, texto secundário

### Entradas

| Campo | Tipo | Obrigatório | Validação | Valor padrão |
|-------|------|-------------|-----------|--------------|
| email | string | sim | Formato RFC 5322 | — |
| senha | string | sim | Mínimo 1 caractere (validação real no backend) | — |

### Interações do Usuário

| Ação | Gatilho | O que acontece |
|------|---------|----------------|
| Clica em "Entrar" | Botão ou Enter | Valida campos, chama POST /auth/login |
| Clica em "Esqueci minha senha" | Link | Navega para /recuperar-senha |
| Toggle de visibilidade da senha | Ícone olho | Alterna type entre password e text |

### Estados da Tela

| Estado | Descrição |
|--------|-----------|
| **Padrão** | Formulário vazio, botão habilitado |
| **Submetendo** | Botão com spinner, campos bloqueados |
| **Erro de validação** | Mensagem inline abaixo do campo |
| **Erro de credencial** | Banner acima do formulário |
| **Bloqueado** | Banner com timer de desbloqueio |

### Chamadas de API

| Método | Endpoint | Momento | Dados enviados | Resposta esperada |
|--------|----------|---------|----------------|-------------------|
| POST | /auth/login | Ao submeter | { email, senha } | { accessToken, refreshToken, perfil } |

---

## Tela: Primeiro Acesso (Convite)

> **Rota:** `/convite/:token`  
> **Autenticação:** Não requerida  
> **Perfis com acesso:** Alunos e responsáveis convidados pela escola

### Contexto

Professor ou admin cadastrou o aluno/responsável e o sistema enviou um e-mail com link de convite. O token tem validade de 72 horas.

### Layout e Componentes

- **Mensagem de boas-vindas** — "Bem-vindo(a) à [Nome da Escola]! Configure sua senha para começar."
- **Campo Nome Completo** — pré-preenchido com o nome cadastrado pelo professor (editável)
- **Campo Nova Senha** — com indicador de força
- **Campo Confirmar Senha**
- **Botão "Criar minha conta"**

### Entradas

| Campo | Tipo | Obrigatório | Validação |
|-------|------|-------------|-----------|
| nome | string | sim | 3–100 caracteres |
| senha | string | sim | Mínimo 8 caracteres, 1 número, 1 maiúscula |
| confirmar_senha | string | sim | Deve ser idêntica ao campo senha |

### Estados da Tela

| Estado | Descrição |
|--------|-----------|
| **Token válido** | Formulário exibido normalmente |
| **Token expirado** | Mensagem: "Este link expirou. Solicite um novo à sua escola." Sem formulário |
| **Token já usado** | Mensagem: "Você já ativou sua conta. Acesse pelo login." Com link para /login |

### Chamadas de API

| Método | Endpoint | Momento | Dados enviados | Resposta esperada |
|--------|----------|---------|----------------|-------------------|
| GET | /auth/convite/:token | Ao montar a tela | — | { nome, email, valido } |
| POST | /auth/convite/:token/ativar | Ao submeter | { nome, senha } | { accessToken, perfil } |

---

## Tela: Recuperação de Senha

> **Rota:** `/recuperar-senha`  
> **Autenticação:** Não requerida

### Fluxo em 2 etapas

**Etapa 1 — Solicitar link:**
- Usuário informa e-mail
- Sistema envia e-mail com link válido por 1 hora (mesmo que o e-mail não exista, exibir mesma mensagem de sucesso — não confirmar se cadastro existe)
- Mensagem: "Se este e-mail estiver cadastrado, você receberá as instruções em breve."

**Etapa 2 — Redefinir senha (rota `/recuperar-senha/:token`):**
- Usuário define nova senha
- Após sucesso: redireciona para /login com toast "Senha redefinida com sucesso!"
- Token expirado: mensagem com link para solicitar novo

### Chamadas de API

| Método | Endpoint | Dados enviados | Resposta esperada |
|--------|----------|----------------|-------------------|
| POST | /auth/recuperar-senha | { email } | 200 OK (sempre, independente de existir) |
| POST | /auth/recuperar-senha/:token | { nova_senha } | 200 OK ou 410 Gone (token expirado) |

---

## Endpoints de Autenticação

### POST /auth/login

> **Autenticação:** Pública  
> **Rate Limit:** 10 requisições por minuto por IP

**Request Body:**
| Campo | Tipo | Obrigatório | Validação |
|-------|------|-------------|-----------|
| email | string | sim | Formato e-mail |
| senha | string | sim | Não vazia |

**Respostas:**

`200 OK`
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 3600,
  "perfil": "professor",
  "usuario": {
    "id": "uuid",
    "nome": "Maria Silva",
    "email": "maria@escola.com.br",
    "avatarUrl": "https://..."
  }
}
```

`401 Unauthorized` — Credenciais inválidas
```json
{ "error": "INVALID_CREDENTIALS", "message": "E-mail ou senha incorretos." }
```

`423 Locked` — Conta bloqueada por tentativas excessivas
```json
{ "error": "ACCOUNT_LOCKED", "message": "Conta bloqueada temporariamente.", "desbloqueiaEm": "2025-01-15T15:00:00Z" }
```

### POST /auth/refresh

> **Autenticação:** Refresh token no cookie httpOnly  
> **Finalidade:** Renovar access token sem nova autenticação

`200 OK` — Retorna novo accessToken  
`401` — Refresh token inválido ou expirado → forçar logout

### POST /auth/logout

> **Autenticação:** Bearer token  
> **Finalidade:** Invalidar refresh token no servidor e limpar cookies

`204 No Content` — Sempre retorna sucesso (idempotente)

### Regras de Negócio — Autenticação

- Access token válido por **1 hora**; refresh token válido por **30 dias**
- Refresh tokens são de uso único (rotation) — ao usar um refresh token, ele é invalidado e um novo é emitido
- Sessões são por dispositivo; o usuário pode ter até 5 sessões simultâneas
- Após 5 tentativas de login falhas em 15 minutos, a conta é bloqueada por 15 minutos
- Professores e administradores só podem ser cadastrados por um Admin da Escola — não há auto-cadastro
