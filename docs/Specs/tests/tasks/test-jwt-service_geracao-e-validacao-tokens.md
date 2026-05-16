# Testes Unitários — JwtService: Geração e Validação de Tokens

> **Escopo:** Backend — `JwtService`  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

`JwtService` é responsável por gerar e validar tokens JWT (access e refresh) utilizados no fluxo de autenticação. É um dos componentes mais críticos do sistema — qualquer falha compromete a segurança de toda a aplicação. Os testes cobrem geração com claims corretas, validade de assinatura, expiração e extração de dados.

---

## O que deve ser implementado

Criar a classe `JwtServiceTest` em `src/test/java/br/com/inovadados/teacherplatform/service/` com testes unitários cobrindo os 4 métodos públicos do serviço. Nenhuma dependência de Spring — usar apenas `ReflectionTestUtils` para injetar os `@Value`.

**Métodos a testar:**
- `generateAccessToken(Usuario)` — claims, validade, não expirado imediatamente
- `generateRefreshToken(Usuario)` — unicidade por chamada, ausência de claim `perfil`
- `extractEmail(String)` — extração correta, falha em token inválido/expirado
- `isTokenValid(String)` — token válido, expirado, assinatura errada, null, vazio, malformado

---

## Critérios de Aceite

- [ ] `generateAccessToken` produz token com `subject = email`, claim `perfil` e claim `usuarioId`
- [ ] `generateAccessToken` produz token válido imediatamente após geração
- [ ] `generateAccessToken` com `accessTokenExpiration = 1s` expira após 2s (`isTokenValid = false`)
- [ ] `generateRefreshToken` gera tokens diferentes a cada chamada (UUID `jti` único)
- [ ] `generateRefreshToken` **não** contém claim `perfil`
- [ ] `extractEmail` retorna o email correto para access e refresh token válidos
- [ ] `extractEmail` lança exceção para token com assinatura adulterada
- [ ] `extractEmail` lança exceção para token expirado
- [ ] `isTokenValid` retorna `false` para: token expirado, assinatura errada, string vazia, `null`, token malformado
- [ ] `isTokenValid` **nunca** lança exceção — apenas retorna `false`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/JwtService.md`
- **Seções:** Todos os métodos — `generateAccessToken`, `generateRefreshToken`, `extractEmail`, `isTokenValid`

---

## Detalhes Técnicos

**Setup da classe de teste:**
```java
// Não usar @SpringBootTest — teste puro com ReflectionTestUtils
JwtService jwtService = new JwtService();
ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET_BASE64_256BITS);
ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900L);
ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 2592000L);
```

**Localização do arquivo de teste:**
`src/test/java/br/com/inovadados/teacherplatform/service/JwtServiceTest.java`

**Dependências de teste necessárias (verificar pom.xml):**
- JUnit 5 (Jupiter)
- `spring-test` (para `ReflectionTestUtils`)
- `io.jsonwebtoken:jjwt` (já presente no projeto)

---

## Notas e Edge Cases

- O secret deve ter pelo menos 256 bits em Base64 para o algoritmo HS256 — usar string de 44+ chars
- Para testar expiração, usar `accessTokenExpiration = -1` (já expirado) ou sleep de 2s
- `isTokenValid(null)` deve retornar `false` sem `NullPointerException`
- O refresh token não deve conter claim `perfil` — verificar ausência explicitamente

---

## Definition of Done

- [ ] Classe `JwtServiceTest` criada em `src/test/java/.../service/`
- [ ] Todos os cenários da spec cobertos (mínimo 16 testes)
- [ ] Testes passam com `./mvnw test -Dtest=JwtServiceTest`
- [ ] Sem dependência de contexto Spring (`@SpringBootTest` não usado)
