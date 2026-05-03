# Plano de Implementação — backend-auth_endpoint-post-login

> **Task origem:** `docs/Tasks/backend-auth_endpoint-post-login.md`
> **Escopo:** Backend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `backend-model_usuario-plan.md` (entidades Usuario e Sessao existentes)

---

## Contexto do Codebase

As entidades `Usuario`, `Sessao`, `Escola` e seus repositories já existem. O projeto ainda não tem Spring Security, JWT, nem Redis configurados. Este plano implementa o endpoint `POST /auth/login` com bcrypt, JWT, cookie httpOnly, rate limiting por IP via Redis e bloqueio por tentativas de login.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- Segurança -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.6</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
</dependency>

<!-- Redis (rate limiting e bloqueio de conta) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### Configurações adicionais em application.properties
```properties
# JWT
app.jwt.secret=<base64-256bits>
app.jwt.access-token-expiration=3600
app.jwt.refresh-token-expiration=2592000

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Rate limit
app.auth.max-attempts=5
app.auth.lockout-minutes=15
app.auth.rate-limit-per-minute=10
```

---

## Arquivos a Criar

### Security Layer

`src/main/java/br/com/inovadados/teacherplatform/security/SecurityConfig.java`
- `@Configuration @EnableWebSecurity`
- Desabilitar CSRF para APIs stateless
- Marcar `/auth/**` como `permitAll()`
- Todas as outras rotas exigem `authenticated()`
- Adicionar `JwtAuthFilter` antes do `UsernamePasswordAuthenticationFilter`
- BCryptPasswordEncoder como bean

`src/main/java/br/com/inovadados/teacherplatform/security/JwtAuthFilter.java`
- `OncePerRequestFilter`
- Ler header `Authorization: Bearer <token>`
- Chamar `JwtService.extractEmail(token)` e carregar `UserDetailsService`
- Setar `SecurityContextHolder` se token válido

`src/main/java/br/com/inovadados/teacherplatform/security/UserDetailsServiceImpl.java`
- Implementa `UserDetailsService`
- `loadUserByUsername(email)` → busca via `UsuarioRepository.findByEmail()`

### Serviços

`src/main/java/br/com/inovadados/teacherplatform/service/JwtService.java`
- `generateAccessToken(Usuario usuario)` → JWT com `sub=email`, `perfil`, `usuarioId`, exp=1h
- `generateRefreshToken(Usuario usuario)` → JWT com `sub=email`, exp=30d, UUID único no claim `jti`
- `extractEmail(String token)` → String
- `isTokenValid(String token)` → boolean
- Assinar com HMAC-SHA256 usando `app.jwt.secret`

`src/main/java/br/com/inovadados/teacherplatform/service/AuthService.java`
- `login(LoginRequest request, HttpServletRequest httpRequest)` → `LoginResponse`
- Fluxo:
  1. Buscar usuario por email; se não encontrar → 401 (mesmo erro que senha errada)
  2. Verificar bloqueio no Redis: `KEY = "login:lock:{email}"`, se existe → 423 com `desbloqueiaEm`
  3. Comparar senha com `BCryptPasswordEncoder`; se errada → incrementar contador Redis `"login:attempts:{email}"` (TTL 15min); se ≥ 5 → criar `"login:lock:{email}"` com TTL 15min → 401 ou 423
  4. Verificar `usuario.ativo`; se false → 403
  5. Gerar accessToken e refreshToken via `JwtService`
  6. Hash do refreshToken com SHA-256 e salvar em `Sessao`
  7. Limpar Redis (attempts e lock)
  8. Retornar `LoginResponse` + Set-Cookie com refreshToken

`src/main/java/br/com/inovadados/teacherplatform/service/RateLimitService.java`
- Usa `RedisTemplate<String, String>`
- `checkRateLimit(String ip)` → lança `TooManyRequestsException` se > 10 req/min
- `incrementAttempts(String email)` → incrementa contador com TTL
- `getLockExpiry(String email)` → `Optional<Instant>`
- `lockAccount(String email)` → seta lock por 15min
- `clearAttempts(String email)` → del contador e lock

### DTOs

`src/main/java/br/com/inovadados/teacherplatform/dto/request/LoginRequest.java`
```java
public record LoginRequest(
  @Email @NotBlank String email,
  @NotBlank String senha
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/LoginResponse.java`
```java
public record LoginResponse(
  String accessToken,
  int expiresIn,
  String perfil,
  UsuarioResponse usuario
) {}
```

`src/main/java/br/com/inovadados/teacherplatform/dto/response/UsuarioResponse.java`
```java
public record UsuarioResponse(UUID id, String nome, String email, String avatarUrl) {}
```

### Controller

`src/main/java/br/com/inovadados/teacherplatform/controller/AuthController.java`
- `@RestController @RequestMapping("/auth")`
- `POST /auth/login` → chama `RateLimitService.checkRateLimit(ip)` → chama `AuthService.login()`
- Monta `ResponseCookie` com atributos httpOnly, Secure, SameSite=Strict, MaxAge=30d
- Retorna 200 com body `LoginResponse`

### Exception Handling

`src/main/java/br/com/inovadados/teacherplatform/exception/GlobalExceptionHandler.java`
- `@RestControllerAdvice`
- Handler para `AccountLockedException` → 423 `{ error: "ACCOUNT_LOCKED", desbloqueiaEm: ISO8601 }`
- Handler para `BadCredentialsException` → 401 `{ error: "INVALID_CREDENTIALS", message: "..." }`
- Handler para `AccountInactiveException` → 403 `{ error: "ACCOUNT_INACTIVE" }`
- Handler para `TooManyRequestsException` → 429

`src/main/java/br/com/inovadados/teacherplatform/exception/AccountLockedException.java`
`src/main/java/br/com/inovadados/teacherplatform/exception/AccountInactiveException.java`
`src/main/java/br/com/inovadados/teacherplatform/exception/TooManyRequestsException.java`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `pom.xml` | Adicionar: spring-boot-starter-security, jjwt-api/impl/jackson, spring-boot-starter-data-redis |
| `application.properties` | Adicionar: app.jwt.*, spring.data.redis.*, app.auth.* |

---

## Ordem de Implementação

1. Adicionar dependências no pom.xml
2. Configurar `application.properties`
3. Criar `JwtService` (independente, testável isoladamente)
4. Criar `RateLimitService` (RedisTemplate)
5. Criar `UserDetailsServiceImpl`
6. Criar `SecurityConfig` (expõe BCryptPasswordEncoder como bean)
7. Criar `JwtAuthFilter`
8. Criar DTOs (LoginRequest, LoginResponse, UsuarioResponse)
9. Criar exceptions customizadas
10. Criar `GlobalExceptionHandler`
11. Criar `AuthService.login()`
12. Criar `AuthController`
13. Testes unitários: JwtService, RateLimitService (com Redis embedded), AuthService (mock repository)
14. Teste de integração: POST /auth/login com banco real

---

## Resumo

- **14 arquivos** a criar
- **2 arquivos** a modificar (pom.xml, application.properties)
- **Bibliotecas aproveitadas:** Spring JPA, Lombok (já existentes)
- **Bibliotecas a adicionar:** spring-security, jjwt 0.12.6, spring-data-redis
- **Complexidade mantida:** M
