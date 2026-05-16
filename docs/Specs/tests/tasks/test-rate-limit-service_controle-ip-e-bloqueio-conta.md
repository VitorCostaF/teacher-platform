# Testes Unitários — RateLimitService: Rate Limit por IP e Bloqueio de Conta

> **Escopo:** Backend — `RateLimitService`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** Nenhuma

---

## Contexto

`RateLimitService` protege o endpoint de login contra ataques de força bruta via dois mecanismos: rate limit por IP (requisições por minuto) e bloqueio temporário de conta após N tentativas falhas. Toda a persistência de contadores usa Redis. Os testes devem mockar `StringRedisTemplate` completamente.

---

## O que deve ser implementado

Criar `RateLimitServiceTest` cobrindo os 5 métodos do serviço. O foco principal é garantir que: (1) o TTL só é definido na primeira contagem, (2) a exceção é lançada no threshold correto, (3) `clearAttempts` remove ambas as chaves Redis.

**Métodos a testar:**
- `checkRateLimit(String ip)` — primeiro request, dentro do limite, acima do limite, Redis null
- `incrementAttempts(String email)` — primeira tentativa (seta TTL), subsequentes, Redis null
- `getLockExpiry(String email)` — sem bloqueio, com bloqueio ativo, Redis null
- `lockAccount(String email)` — chave correta com TTL
- `clearAttempts(String email)` — ambas as chaves removidas

---

## Critérios de Aceite

- [ ] `checkRateLimit` chama `expire(1 minuto)` apenas quando `count == 1`
- [ ] `checkRateLimit` lança `TooManyRequestsException` quando `count > rateLimitPerMinute`
- [ ] `checkRateLimit` com Redis retornando `null` não lança exceção
- [ ] `incrementAttempts` chama `expire(lockoutMinutes)` apenas na primeira tentativa (`count == 1`)
- [ ] `incrementAttempts` com Redis `null` retorna `1` (fallback)
- [ ] `getLockExpiry` retorna `Optional.empty()` quando TTL <= 0 ou `null`
- [ ] `getLockExpiry` retorna `Optional<Instant>` com `now + TTL` quando conta bloqueada
- [ ] `lockAccount` salva com chave `"login:lock:{email}"` e TTL = `lockoutMinutes`
- [ ] `clearAttempts` chama `delete` exatamente 2 vezes: chave de tentativas e de lock
- [ ] Chave de rate limit usa prefixo `"rate:{ip}"`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/RateLimitService.md`
- **Seções:** Todos os métodos documentados na spec

---

## Detalhes Técnicos

**Setup da classe de teste:**
```java
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", 5);
        ReflectionTestUtils.setField(rateLimitService, "lockoutMinutes", 15L);
        ReflectionTestUtils.setField(rateLimitService, "rateLimitPerMinute", 10L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }
}
```

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/RateLimitServiceTest.java`

---

## Notas e Edge Cases

- `expire` é chamado via `redisTemplate.expire(key, Duration)` — não via `valueOps`
- `getExpire` usa `TimeUnit.SECONDS` — verificar se o mock retorna `Long`
- Testar que o método `delete` é chamado com os dois prefixos distintos: `"login:attempts:"` e `"login:lock:"`

---

## Definition of Done

- [ ] Classe `RateLimitServiceTest` criada
- [ ] Todos os cenários da spec cobertos (mínimo 14 testes)
- [ ] Testes passam com `./mvnw test -Dtest=RateLimitServiceTest`
- [ ] Sem chamadas reais ao Redis
