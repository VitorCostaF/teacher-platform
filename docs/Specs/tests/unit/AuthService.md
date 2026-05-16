# Spec de Testes Unitários — AuthService

**Classe:** `br.com.inovadados.teacherplatform.service.AuthService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/AuthService.java`

---

## Visão Geral

`AuthService` orquestra o fluxo completo de autenticação: login, refresh e logout de sessões. É o serviço com mais regras de segurança combinadas — credenciais, bloqueio de conta, rotação de tokens e limite de sessões simultâneas.

**Dependências para mock:** `UsuarioRepository`, `SessaoRepository`, `JwtService`, `RateLimitService`, `PasswordEncoder`.

---

## Método: `login(LoginRequest, HttpServletRequest)`

### Fluxo feliz

| # | Cenário | Condições | Saída Esperada |
|---|---------|-----------|----------------|
| 1 | Login bem-sucedido | usuário ativo, senha correta, sem lock | retorna `AuthLoginResult` com `loginResponse` e `refreshToken` |
| 2 | `accessToken` gerado | login ok | `jwtService.generateAccessToken` chamado 1 vez |
| 3 | `refreshToken` gerado | login ok | `jwtService.generateRefreshToken` chamado 1 vez |
| 4 | Sessão salva | login ok | `sessaoRepository.save` chamado 1 vez |
| 5 | Tentativas limpas após sucesso | login ok | `rateLimitService.clearAttempts` chamado com o email |
| 6 | `ultimoAcesso` atualizado | login ok | `usuario.ultimoAcesso` != null após login |
| 7 | `loginResponse` contém perfil | login ok, `perfil=PROFESSOR` | `loginResponse.perfil() == "PROFESSOR"` |

### Erros de credencial

| # | Cenário | Condições | Exceção Esperada |
|---|---------|-----------|-----------------|
| 8 | Email não cadastrado | `usuarioRepository.findByEmail` retorna empty | `BadCredentialsException` |
| 9 | Senha errada, abaixo do limite | `passwordEncoder.matches = false`, tentativas=1 | `BadCredentialsException`; `incrementAttempts` chamado |
| 10 | Senha errada, atinge limite | tentativas >= `maxAttempts` | `AccountLockedException`; `lockAccount` chamado |
| 11 | Conta inativa | usuário com `ativo=false`, senha certa | `AccountInactiveException` |

### Bloqueio de conta

| # | Cenário | Condições | Exceção Esperada |
|---|---------|-----------|-----------------|
| 12 | Conta já bloqueada | `rateLimitService.getLockExpiry` retorna `Optional.of(Instant)` | `AccountLockedException` antes de verificar senha |
| 13 | Expiry retornado corretamente | lock ativo por 15 min | `AccountLockedException` com `expiry` próximo de `now + 15min` |

### Sessão construída

| # | Cenário | Dado | Valor Esperado na Sessão |
|---|---------|------|--------------------------|
| 14 | Hash do refresh token | token = `"abc"` | `sessao.refreshTokenHash == SHA-256("abc")` em Base64 |
| 15 | IP extraído de `X-Forwarded-For` | header `"1.2.3.4, 5.6.7.8"` | `sessao.ip == "1.2.3.4"` |
| 16 | IP extraído do `remoteAddr` | sem `X-Forwarded-For` | `sessao.ip == httpRequest.getRemoteAddr()` |
| 17 | `expiraEm` calculada corretamente | `refreshTokenExpiration = 2592000` | `sessao.expiraEm ≈ now + 30 dias` |

---

## Método: `refresh(String rawRefreshToken)`

| # | Cenário | Condições | Saída / Exceção |
|---|---------|-----------|-----------------|
| 1 | Token nulo | `rawRefreshToken = null` | lança `UnauthorizedException` |
| 2 | Token vazio | `rawRefreshToken = ""` | lança `UnauthorizedException` |
| 3 | Token inválido/expirado | `jwtService.isTokenValid = false` | lança `UnauthorizedException` |
| 4 | Sessão não encontrada | `sessaoRepository.findByRefreshTokenHash...` retorna empty | lança `UnauthorizedException` |
| 5 | Refresh bem-sucedido — nova sessão criada | sessão válida | `sessaoRepository.save` chamado 2 vezes (revoga + cria) |
| 6 | Sessão atual revogada | refresh ok | `sessaoAtual.revogadoEm` != null |
| 7 | Novo access token gerado | refresh ok | `jwtService.generateAccessToken` chamado 1 vez |
| 8 | Novo refresh token gerado | refresh ok | `jwtService.generateRefreshToken` chamado 1 vez |
| 9 | Retorna `AuthLoginResult` | refresh ok | `loginResponse` e novo `refreshToken` |

### Limite de sessões simultâneas

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 10 | Menos de 5 sessões ativas | 3 sessões | nenhuma revogada além da atual |
| 11 | Exatamente 5 sessões ativas | 5 sessões, mais antiga ≠ atual | sessão mais antiga revogada |
| 12 | 5 sessões ativas, mais antiga = atual | sessão mais antiga é a própria atual | não revoga a mais antiga (evita auto-revogação) |

---

## Método: `logout(String rawRefreshToken)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Token nulo | `rawRefreshToken = null` | não faz nada (sem exceção) |
| 2 | Token em branco | `rawRefreshToken = ""` | não faz nada |
| 3 | Sessão válida | `sessaoRepository.findBy...` retorna sessão | `sessao.revogadoEm` preenchido; `sessaoRepository.save` chamado |
| 4 | Sessão não encontrada | `sessaoRepository.findBy...` retorna empty | não faz nada (sem exceção) |
| 5 | Sessão já revogada | repo não retorna (condição `revogadoEmIsNull`) | não faz nada |

---

## Método: `hashSha256` (private — verificar via comportamento do `login`)

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Hash determinístico | mesmo token em 2 chamadas | mesmo hash nas duas |
| 2 | Hashes diferentes | tokens diferentes | hashes diferentes |
| 3 | Sessão salva com hash correto | token = `"t"` | `sessao.refreshTokenHash == Base64(SHA256("t"))` |

---

## Regras de Negócio Críticas

- A senha é verificada com `passwordEncoder.matches(raw, hash)` — nunca comparação direta.
- O refresh token é armazenado **hasheado** (SHA-256 + Base64) — nunca em plain text.
- O bloqueio é verificado **antes** da senha — não revela se o usuário existe.
- O refresh invalida a sessão atual e cria uma nova (rotação de token).
- Usuário inativo não pode logar, mesmo com credenciais corretas.
- `clearAttempts` só é chamado após login **bem-sucedido**.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock SessaoRepository sessaoRepository;
    @Mock JwtService jwtService;
    @Mock RateLimitService rateLimitService;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900);
        ReflectionTestUtils.setField(authService, "maxAttempts", 5);
    }

    private Usuario usuarioAtivo() {
        var u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setEmail("user@escola.com");
        u.setSenhaHash("$2a$hash");
        u.setAtivo(true);
        u.setPerfil(PerfilEnum.PROFESSOR);
        return u;
    }

    private HttpServletRequest httpRequestFake(String ip) {
        var req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn(ip);
        return req;
    }
}
```
