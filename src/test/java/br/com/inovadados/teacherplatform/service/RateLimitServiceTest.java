package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    @InjectMocks
    RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", 5);
        ReflectionTestUtils.setField(rateLimitService, "lockoutMinutes", 15L);
        ReflectionTestUtils.setField(rateLimitService, "rateLimitPerMinute", 10L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // --- checkRateLimit ---

    @Test
    void checkRateLimit_primeiroRequest_deveDefinirTTLDeUmMinuto() {
        when(valueOps.increment("rate:192.168.0.1")).thenReturn(1L);
        rateLimitService.checkRateLimit("192.168.0.1");
        verify(redisTemplate).expire("rate:192.168.0.1", Duration.ofMinutes(1));
    }

    @Test
    void checkRateLimit_requestsDentroDoLimite_naoDeveLancarExcecao() {
        when(valueOps.increment("rate:192.168.0.1")).thenReturn(5L);
        assertDoesNotThrow(() -> rateLimitService.checkRateLimit("192.168.0.1"));
    }

    @Test
    void checkRateLimit_requestsAcimaDoLimite_deveLancarTooManyRequestsException() {
        when(valueOps.increment("rate:192.168.0.1")).thenReturn(11L);
        assertThrows(TooManyRequestsException.class, () -> rateLimitService.checkRateLimit("192.168.0.1"));
    }

    @Test
    void checkRateLimit_redisRetornandoNull_naoDeveLancarExcecao() {
        when(valueOps.increment("rate:192.168.0.1")).thenReturn(null);
        assertDoesNotThrow(() -> rateLimitService.checkRateLimit("192.168.0.1"));
    }

    @Test
    void checkRateLimit_naoDeveDefinirTTLEmRequestsSubsequentes() {
        when(valueOps.increment("rate:192.168.0.1")).thenReturn(5L);
        rateLimitService.checkRateLimit("192.168.0.1");
        verify(redisTemplate, never()).expire(eq("rate:192.168.0.1"), any(Duration.class));
    }

    @Test
    void checkRateLimit_chaveDeveUsarPrefixoRateIp() {
        when(valueOps.increment("rate:10.0.0.1")).thenReturn(1L);
        rateLimitService.checkRateLimit("10.0.0.1");
        verify(valueOps).increment("rate:10.0.0.1");
    }

    // --- incrementAttempts ---

    @Test
    void incrementAttempts_primeiraTentativa_deveDefinirTTL() {
        when(valueOps.increment("login:attempts:user@email.com")).thenReturn(1L);
        rateLimitService.incrementAttempts("user@email.com");
        verify(redisTemplate).expire("login:attempts:user@email.com", Duration.ofMinutes(15L));
    }

    @Test
    void incrementAttempts_tentativasSubsequentes_naoDeveDefinirTTL() {
        when(valueOps.increment("login:attempts:user@email.com")).thenReturn(3L);
        rateLimitService.incrementAttempts("user@email.com");
        verify(redisTemplate, never()).expire(eq("login:attempts:user@email.com"), any(Duration.class));
    }

    @Test
    void incrementAttempts_redisRetornandoNull_deveRetornar1() {
        when(valueOps.increment("login:attempts:user@email.com")).thenReturn(null);
        long resultado = rateLimitService.incrementAttempts("user@email.com");
        assertEquals(1L, resultado);
    }

    @Test
    void incrementAttempts_deveRetornarContadorCorreto() {
        when(valueOps.increment("login:attempts:user@email.com")).thenReturn(4L);
        long resultado = rateLimitService.incrementAttempts("user@email.com");
        assertEquals(4L, resultado);
    }

    // --- getLockExpiry ---

    @Test
    void getLockExpiry_semBloqueio_deveRetornarEmpty() {
        when(redisTemplate.getExpire("login:lock:user@email.com", TimeUnit.SECONDS)).thenReturn(0L);
        Optional<Instant> resultado = rateLimitService.getLockExpiry("user@email.com");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void getLockExpiry_redisNull_deveRetornarEmpty() {
        when(redisTemplate.getExpire("login:lock:user@email.com", TimeUnit.SECONDS)).thenReturn(null);
        Optional<Instant> resultado = rateLimitService.getLockExpiry("user@email.com");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void getLockExpiry_contaBloqueada_deveRetornarInstantNoFuturo() {
        when(redisTemplate.getExpire("login:lock:user@email.com", TimeUnit.SECONDS)).thenReturn(300L);
        Optional<Instant> resultado = rateLimitService.getLockExpiry("user@email.com");
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().isAfter(Instant.now()));
    }

    @Test
    void getLockExpiry_ttlNegativo_deveRetornarEmpty() {
        when(redisTemplate.getExpire("login:lock:user@email.com", TimeUnit.SECONDS)).thenReturn(-1L);
        Optional<Instant> resultado = rateLimitService.getLockExpiry("user@email.com");
        assertTrue(resultado.isEmpty());
    }

    // --- lockAccount ---

    @Test
    void lockAccount_deveSalvarComChaveCorretaETTL() {
        rateLimitService.lockAccount("user@email.com");
        verify(valueOps).set("login:lock:user@email.com", "1", Duration.ofMinutes(15L));
    }

    // --- clearAttempts ---

    @Test
    void clearAttempts_deveDeletarChaveDeAttempts() {
        rateLimitService.clearAttempts("user@email.com");
        verify(redisTemplate).delete("login:attempts:user@email.com");
    }

    @Test
    void clearAttempts_deveDeletarChaveDeLock() {
        rateLimitService.clearAttempts("user@email.com");
        verify(redisTemplate).delete("login:lock:user@email.com");
    }

    @Test
    void clearAttempts_deveDeletarAmbasAsChaves() {
        rateLimitService.clearAttempts("user@email.com");
        verify(redisTemplate, times(2)).delete(anyString());
    }
}
