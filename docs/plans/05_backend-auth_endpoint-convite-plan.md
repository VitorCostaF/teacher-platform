# Plano de Implementação — backend-auth_endpoint-convite

> **Task origem:** `docs/Tasks/backend-auth_endpoint-convite.md`
> **Escopo:** Backend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `backend-model_usuario-plan.md` (TokenTemporario, Usuario existentes)

---

## Contexto do Codebase

`TokenTemporarioRepository`, `UsuarioRepository`, `AuthController`, `AuthService`, `JwtService` e o `GlobalExceptionHandler` já existem do plano de login. Este plano adiciona quatro endpoints ao `AuthController` existente, além de criar um `EmailService` para envio de e-mails transacionais.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- E-mail -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### Configurações em application.properties
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.frontend.url=https://app.teacherplatform.com.br
app.convite.expiracao-horas=72
app.recuperacao-senha.expiracao-horas=1
```

---

## Arquivos a Criar

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/EmailService.java`
- Injeta `JavaMailSender`
- `sendConviteEmail(String toEmail, String nome, String token)` → monta HTML com link `${app.frontend.url}/primeiro-acesso?token={token}`
- `sendRecuperacaoSenhaEmail(String toEmail, String token)` → link `${app.frontend.url}/recuperar-senha?token={token}`
- Executar envio de forma assíncrona (`@Async`) para não bloquear a resposta

`src/main/java/br/com/inovadados/teacherplatform/service/TokenService.java`
- `gerarTokenConvite(Usuario usuario)` → cria `TokenTemporario` com `tipo=CONVITE`, token UUID aleatório (raw), hash SHA-256, expira em 72h; persiste; retorna token raw
- `gerarTokenRecuperacaoSenha(Usuario usuario)` → cria `TokenTemporario` com `tipo=RECUPERACAO_SENHA`, exp 1h
- `validarToken(String tokenRaw, TipoTokenEnum tipo)` → busca por hash, valida expiração e `usadoEm == null`; lança `TokenExpiradoException` ou `TokenJaUsadoException`
- `marcarTokenUsado(TokenTemporario token)` → seta `usadoEm = now()`

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/response/ConviteResponse.java`
```java
public record ConviteResponse(String nome, String email, boolean valido) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/AtivarContaRequest.java`
```java
public record AtivarContaRequest(
  @Size(min=3, max=100) @NotBlank String nome,
  @Size(min=8) @NotBlank String senha,
  @NotBlank String confirmarSenha
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/SolicitarRecuperacaoRequest.java`
```java
public record SolicitarRecuperacaoRequest(@Email @NotBlank String email) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/request/RedefinirSenhaRequest.java`
```java
public record RedefinirSenhaRequest(
  @Size(min=8) @NotBlank String senha,
  @NotBlank String confirmarSenha
) {}
```

### Exceptions

`src/main/java/br/com/inovadados/teacherplatform/exception/TokenExpiradoException.java`

`src/main/java/br/com/inovadados/teacherplatform/exception/TokenJaUsadoException.java`

---

## Arquivos a Modificar

### `AuthController.java`

Adicionar quatro endpoints:

**GET /auth/convite/{token}**
- Chama `tokenService.validarToken(token, CONVITE)`
- 200 → `ConviteResponse(nome, email, true)`
- `TokenExpiradoException` → 410
- `TokenJaUsadoException` → 409

**POST /auth/convite/{token}/ativar**
- Validar `request.senha.equals(request.confirmarSenha)` → 400 se não
- Buscar usuário via token
- Atualizar `usuario.nome` e `usuario.senhaHash` (BCrypt)
- Marcar token como usado
- Criar sessão e retornar `LoginResponse` (igual ao login bem-sucedido)

**POST /auth/recuperar-senha**
- Buscar usuário por email; se não encontrar → 200 mesmo assim (não revelar existência)
- Se encontrar: `tokenService.gerarTokenRecuperacaoSenha()` + `emailService.sendRecuperacaoSenhaEmail()`
- Retornar 200 `{ message: "Se o e-mail existir, você receberá as instruções." }`

**POST /auth/recuperar-senha/{token}**
- Validar token (tipo RECUPERACAO_SENHA, 1h)
- Validar `request.senha.equals(request.confirmarSenha)`
- Atualizar `usuario.senhaHash`
- Marcar token como usado
- Invalidar todas as sessões ativas do usuário (revogar todas via `SessaoRepository`)
- 200 em sucesso, `TokenExpiradoException` → 410

### `AuthService.java`

Adicionar os métodos:
- `ativarConta(String token, AtivarContaRequest request, HttpServletResponse response)` → `LoginResponse`
- `solicitarRecuperacaoSenha(String email)` → void
- `redefinirSenha(String token, RedefinirSenhaRequest request)` → void

### `GlobalExceptionHandler.java`

Adicionar:
- `TokenExpiradoException` → 410 `{ error: "TOKEN_EXPIRED" }`
- `TokenJaUsadoException` → 409 `{ error: "TOKEN_ALREADY_USED" }`

### `pom.xml`

Adicionar spring-boot-starter-mail.

---

## Validação de Senha

Regex server-side para validar: ao menos 8 chars, 1 maiúscula, 1 número:
```java
// Em AtivarContaRequest ou no serviço
if (!senha.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) throw new InvalidPasswordException();
```

---

## Ordem de Implementação

1. Adicionar spring-boot-starter-mail ao pom.xml e configurar application.properties
2. Criar exceptions `TokenExpiradoException`, `TokenJaUsadoException`
3. Criar `TokenService`
4. Criar `EmailService` (com `@Async` — requer `@EnableAsync` na main class ou config)
5. Criar DTOs
6. Implementar métodos em `AuthService`
7. Adicionar endpoints em `AuthController`
8. Atualizar `GlobalExceptionHandler`
9. Testes: token expirado, token já usado, senha inválida, e-mail inexistente retorna 200

---

## Resumo

- **7 arquivos** a criar (TokenService, EmailService, 4 DTOs, 2 exceptions)
- **4 arquivos** a modificar: AuthController, AuthService, GlobalExceptionHandler, pom.xml
- **Bibliotecas a adicionar:** spring-boot-starter-mail
- **Complexidade mantida:** M
