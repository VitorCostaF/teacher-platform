# Plano de Implementação — backend-auth_endpoint-refresh-logout

> **Task origem:** `docs/Tasks/backend-auth_endpoint-refresh-logout.md`
> **Escopo:** Backend — Autenticação
> **Complexidade:** M
> **Sprint:** 1 — Autenticação
> **Depende de:** `backend-auth_endpoint-post-login-plan.md` (JwtService, AuthService, AuthController, Sessao já existentes)

---

## Contexto do Codebase

`AuthController`, `AuthService`, `JwtService`, `SessaoRepository` e toda a infraestrutura de segurança criada na task anterior já existem. Este plano adiciona apenas dois endpoints: `POST /auth/refresh` e `POST /auth/logout`, estendendo o `AuthController` e `AuthService` existentes.

---

## Dependências

Nenhuma nova dependência. Tudo já disponível: Spring Security, jjwt, Redis, JPA.

---

## Arquivos a Criar

Nenhum arquivo novo é necessário — toda a lógica é adicionada nos arquivos existentes.

---

## Arquivos a Modificar

### `AuthController.java`

Adicionar dois endpoints:

**POST /auth/refresh**
```java
@PostMapping("/refresh")
public ResponseEntity<LoginResponse> refresh(
    @CookieValue(name = "refreshToken", required = false) String refreshToken,
    HttpServletResponse response) {
    // ...chama authService.refresh(refreshToken, response)
}
```

**POST /auth/logout**
```java
@PostMapping("/logout")
public ResponseEntity<Void> logout(
    @CookieValue(name = "refreshToken", required = false) String refreshToken,
    HttpServletResponse response) {
    // ...chama authService.logout(refreshToken, response)
    // Sempre 204
}
```

### `AuthService.java`

**Método `refresh(String rawRefreshToken, HttpServletResponse response)`:**
1. Se `rawRefreshToken` é null ou inválido → lançar `UnauthorizedException`
2. Validar assinatura e expiração via `JwtService.isTokenValid()`
3. Hash SHA-256 do token → buscar `Sessao` no banco via `SessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull()`
4. Se não encontrado (token revogado ou inexistente) → `UnauthorizedException`
5. Verificar quantas sessões ativas o usuário tem via `SessaoRepository.findByUsuarioIdAndRevogadoEmIsNull()`; se ≥ 5, revogar a mais antiga
6. Revogar a sessão atual (setar `revogadoEm = now()`) — rotation: token de uso único
7. Gerar novo accessToken e novo refreshToken via `JwtService`
8. Hash do novo refreshToken e persistir nova `Sessao`
9. Setar novo cookie httpOnly
10. Retornar `LoginResponse` com novo accessToken

**Método `logout(String rawRefreshToken, HttpServletResponse response)`:**
1. Se `rawRefreshToken` não é null: hash SHA-256 → buscar Sessao → setar `revogadoEm = now()`
2. Limpar cookie: `Set-Cookie: refreshToken=; Max-Age=0; HttpOnly; Secure; SameSite=Strict`
3. Retornar 204 sempre (idempotente — não lançar exceção se token ausente ou inválido)

### `SessaoRepository.java`

Adicionar:
```java
List<Sessao> findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc(UUID usuarioId);
```

### `GlobalExceptionHandler.java`

Adicionar handler para `UnauthorizedException` já existente (ou criar se não existir):
```java
@ExceptionHandler(UnauthorizedException.class)
public ResponseEntity<?> handleUnauthorized(UnauthorizedException e) {
    return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED", "message", e.getMessage()));
}
```

---

## Lógica de Hashing do Refresh Token

Em `AuthService`, o hash deve usar SHA-256 idêntico ao criado no login:
```java
private String hashToken(String rawToken) {
    return DigestUtils.sha256Hex(rawToken); // org.apache.commons:commons-codec, ou java.security.MessageDigest
}
```

Usar `java.security.MessageDigest` (sem dependência extra, já na JDK 21).

---

## Ordem de Implementação

1. Adicionar `findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc` no `SessaoRepository`
2. Implementar `AuthService.refresh()`
3. Implementar `AuthService.logout()`
4. Adicionar endpoints em `AuthController`
5. Verificar handler de `UnauthorizedException` no `GlobalExceptionHandler`
6. Testes unitários: cenários de token revogado, expirado, inválido, ausente
7. Teste: limite de 5 sessões simultâneas (mais antiga é revogada)
8. Teste: logout com cookie ausente retorna 204

---

## Resumo

- **0 arquivos** a criar
- **4 arquivos** a modificar: `AuthController`, `AuthService`, `SessaoRepository`, `GlobalExceptionHandler`
- **Complexidade mantida:** M
