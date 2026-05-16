# Spec de Testes Unitários — RateLimitService

**Classe:** `br.com.inovadados.teacherplatform.service.RateLimitService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/RateLimitService.java`

---

## Visão Geral

`RateLimitService` controla dois mecanismos de proteção via Redis:
1. **Rate limit por IP** — limita requisições por minuto por endereço IP.
2. **Bloqueio por tentativas** — bloqueia conta após N falhas de login.

**Dependências para mock:** `StringRedisTemplate` (mock completo com Mockito).

---

## Configuração do Teste

```java
@Value("${app.auth.max-attempts}")         // ex: 5
@Value("${app.auth.lockout-minutes}")      // ex: 15
@Value("${app.auth.rate-limit-per-minute}") // ex: 10
```

Usar `@ExtendWith(MockitoExtension.class)` com `@Mock StringRedisTemplate`.

---

## Métodos Testáveis

### `checkRateLimit(String ip)`

| # | Cenário | Comportamento do Mock Redis | Saída Esperada |
|---|---------|----------------------------|----------------|
| 1 | Primeira requisição do IP | `increment` retorna `1` | não lança exceção; `expire` chamado com 1 minuto |
| 2 | Requisição dentro do limite | `increment` retorna `rateLimitPerMinute` | não lança exceção |
| 3 | Requisição acima do limite | `increment` retorna `rateLimitPerMinute + 1` | lança `TooManyRequestsException` |
| 4 | Redis retorna `null` para increment | `increment` retorna `null` | não lança exceção (tratamento defensivo) |

**Verificações adicionais:**
- No cenário 1 (`count == 1`), `expire` deve ser chamado com `Duration.ofMinutes(1)`.
- Nos demais cenários, `expire` **não** deve ser chamado novamente.

---

### `incrementAttempts(String email)`

| # | Cenário | Comportamento do Mock Redis | Saída Esperada |
|---|---------|----------------------------|----------------|
| 1 | Primeira tentativa falha | `increment` retorna `1` | retorna `1`; `expire` chamado com `lockoutMinutes` |
| 2 | Segunda tentativa falha | `increment` retorna `2` | retorna `2`; `expire` **não** chamado novamente |
| 3 | N-ésima tentativa | `increment` retorna `N` | retorna `N` |
| 4 | Redis retorna `null` | `increment` retorna `null` | retorna `1` (fallback defensivo) |

---

### `getLockExpiry(String email)`

| # | Cenário | Comportamento do Mock Redis | Saída Esperada |
|---|---------|----------------------------|----------------|
| 1 | Conta não bloqueada (sem chave) | `getExpire` retorna `-2` (key inexistente) | `Optional.empty()` |
| 2 | Conta não bloqueada (TTL zero) | `getExpire` retorna `0` | `Optional.empty()` |
| 3 | Conta bloqueada com TTL válido | `getExpire` retorna `900` (15 min) | `Optional` com `Instant` ≈ `now + 900s` |
| 4 | Redis retorna `null` | `getExpire` retorna `null` | `Optional.empty()` |

---

### `lockAccount(String email)`

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Bloqueia conta corretamente | `email = "x@y.com"` | `set("login:lock:x@y.com", "1", lockoutMinutes)` chamado |
| 2 | Chave correta | qualquer email | chave usa prefixo `"login:lock:"` |

---

### `clearAttempts(String email)`

| # | Cenário | Entrada | Saída Esperada |
|---|---------|---------|----------------|
| 1 | Remove tentativas e lock | `email = "x@y.com"` | `delete("login:attempts:x@y.com")` chamado |
| 2 | Remove lock junto | `email = "x@y.com"` | `delete("login:lock:x@y.com")` chamado |
| 3 | Ambas as chaves removidas | qualquer email | `delete` chamado exatamente 2 vezes |

---

## Regras de Negócio Críticas

- O TTL do rate limit por IP é sempre **1 minuto**, reiniciado apenas na primeira requisição.
- O TTL de bloqueio de conta é `lockoutMinutes` — configurável.
- `clearAttempts` deve remover **ambas** as chaves: tentativas e lock.
- Nenhum método deve propagar exceções do Redis — falha silenciosa é aceitável para `increment null`.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", 5);
        ReflectionTestUtils.setField(rateLimitService, "lockoutMinutes", 15L);
        ReflectionTestUtils.setField(rateLimitService, "rateLimitPerMinute", 10L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }
}
```
