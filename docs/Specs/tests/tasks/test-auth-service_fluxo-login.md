# Testes Unitários — AuthService: Fluxo de Login

> **Escopo:** Backend — `AuthService.login`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

O método `login` do `AuthService` orquestra a autenticação completa: verificação de credenciais, bloqueio de conta por tentativas, geração de tokens JWT e persistência da sessão. É o ponto de entrada principal do sistema — qualquer regressão aqui compromete o acesso de todos os usuários.

---

## O que deve ser implementado

Criar `AuthServiceTest` (ou adicionar a ela) com testes cobrindo especificamente o método `login`. Incluir todos os cenários de erro, o caminho feliz e a construção correta da sessão.

**Cenários do `login`:**
- Caminho feliz: tokens gerados, sessão salva, tentativas limpas, `ultimoAcesso` atualizado
- Email não cadastrado → `BadCredentialsException`
- Senha errada (abaixo do limite) → `BadCredentialsException` + `incrementAttempts`
- Senha errada (atinge limite) → `AccountLockedException` + `lockAccount`
- Conta já bloqueada → `AccountLockedException` antes de verificar senha
- Usuário inativo → `AccountInactiveException`
- Sessão construída com hash SHA-256, IP correto e `expiraEm` calculado

---

## Critérios de Aceite

- [ ] Login bem-sucedido retorna `AuthLoginResult` com `loginResponse` e `refreshToken`
- [ ] `jwtService.generateAccessToken` e `generateRefreshToken` chamados 1 vez cada
- [ ] `sessaoRepository.save` chamado 1 vez após login bem-sucedido
- [ ] `rateLimitService.clearAttempts(email)` chamado após login bem-sucedido
- [ ] `usuario.ultimoAcesso` preenchido após login
- [ ] `loginResponse.perfil()` retorna o nome do perfil do usuário
- [ ] Email inexistente lança `BadCredentialsException`
- [ ] Senha errada com tentativas < max lança `BadCredentialsException` e chama `incrementAttempts`
- [ ] Senha errada atingindo o limite lança `AccountLockedException` e chama `lockAccount`
- [ ] Conta bloqueada (`getLockExpiry` retorna presente) lança `AccountLockedException` sem checar senha
- [ ] Usuário inativo lança `AccountInactiveException`
- [ ] `sessao.refreshTokenHash` é SHA-256 em Base64 do refresh token
- [ ] `sessao.ip` extrai primeiro IP do header `X-Forwarded-For` quando presente
- [ ] `sessao.ip` usa `remoteAddr` quando `X-Forwarded-For` ausente
- [ ] `sessao.expiraEm ≈ now + refreshTokenExpiration`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AuthService.md`
- **Seção:** `Método: login` — todas as subseções

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AuthServiceTest.java`

**Setup:**
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
}
```

---

## Notas e Edge Cases

- O bloqueio é verificado **antes** da senha — não revelar se o usuário existe
- `hashSha256` é private — validar indiretamente pelo valor de `sessao.refreshTokenHash`
- Mockar `HttpServletRequest` com `mock(HttpServletRequest.class)` para controlar IP

---

## Definition of Done

- [ ] Todos os cenários do fluxo `login` cobertos (mínimo 15 testes)
- [ ] Testes passam com `./mvnw test -Dtest=AuthServiceTest`
- [ ] Sem chamada real a banco ou Redis
