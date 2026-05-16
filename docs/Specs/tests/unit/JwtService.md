# Spec de Testes Unitários — JwtService

**Classe:** `br.com.inovadados.teacherplatform.service.JwtService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/JwtService.java`

---

## Visão Geral

`JwtService` é responsável por gerar e validar tokens JWT (access e refresh). Os testes devem cobrir a geração com claims corretas, validação de assinatura, expiração e extração de dados.

**Dependências para mock:** nenhuma (apenas `@Value` do Spring — injetar via reflection ou `ReflectionTestUtils`).

---

## Configuração do Teste

```java
@Value("${app.jwt.secret}")        // base64 de pelo menos 256 bits
@Value("${app.jwt.access-token-expiration}")   // ex: 900 (segundos)
@Value("${app.jwt.refresh-token-expiration}")  // ex: 2592000 (segundos)
```

Usar `ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET_BASE64)` para injetar valores nos testes.

---

## Métodos Testáveis

### `generateAccessToken(Usuario usuario)`

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Token gerado com subject correto | `usuario.email = "prof@escola.com"` | `extractEmail(token) == "prof@escola.com"` |
| 2 | Token contém claim `perfil` | `usuario.perfil = PROFESSOR` | claim `perfil` == `"PROFESSOR"` |
| 3 | Token contém claim `usuarioId` | `usuario.id = UUID` | claim `usuarioId` == `usuario.id.toString()` |
| 4 | Token não está expirado imediatamente após geração | qualquer usuário válido | `isTokenValid(token) == true` |
| 5 | Token expira conforme configuração | `accessTokenExpiration = 1` (1 segundo) | após 2s, `isTokenValid(token) == false` |

---

### `generateRefreshToken(Usuario usuario)`

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Token gerado com subject correto | `usuario.email = "aluno@escola.com"` | `extractEmail(token) == "aluno@escola.com"` |
| 2 | Cada chamada gera token único | mesmo usuário, 2 chamadas | os dois tokens são diferentes (UUID aleatório no `jti`) |
| 3 | Token é válido imediatamente | qualquer usuário | `isTokenValid(token) == true` |
| 4 | Token NÃO contém claim `perfil` | qualquer usuário | parsing do token não tem claim `perfil` |

---

### `extractEmail(String token)`

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Extrai email de access token válido | token gerado com `email = "x@y.com"` | `"x@y.com"` |
| 2 | Extrai email de refresh token válido | token gerado com `email = "a@b.com"` | `"a@b.com"` |
| 3 | Token inválido/adulterado | token com assinatura modificada | lança exceção (JwtException) |
| 4 | Token expirado | token com `exp` no passado | lança exceção (ExpiredJwtException) |

---

### `isTokenValid(String token)`

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Token válido | token recém-gerado | `true` |
| 2 | Token com assinatura errada | token assinado com secret diferente | `false` |
| 3 | Token expirado | token com `accessTokenExpiration = -1` (já expirado) | `false` |
| 4 | String vazia | `""` | `false` |
| 5 | `null` | `null` | `false` |
| 6 | Token malformado | `"nao.e.um.jwt"` | `false` |

---

## Regras de Negócio Críticas

- O access token deve conter: `subject` (email), `perfil`, `usuarioId`, `iat`, `exp`.
- O refresh token deve conter: `subject` (email), `jti` (UUID único), `iat`, `exp`. **Não deve** conter `perfil`.
- `isTokenValid` nunca deve lançar exceção — apenas retornar `false` em caso de erro.
- A chave de assinatura usa HMAC-SHA256 com o secret em Base64.

---

## Exemplo de Setup

```java
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "dGVzdC1zZWNyZXQtY2hhdmUtcGFyYS10ZXN0ZXMtdW5pdGFyaW9z"; // 256 bits em base64

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 2592000L);
    }

    private Usuario usuarioFake() {
        var u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setEmail("teste@escola.com");
        u.setPerfil(PerfilEnum.PROFESSOR);
        return u;
    }
}
```
