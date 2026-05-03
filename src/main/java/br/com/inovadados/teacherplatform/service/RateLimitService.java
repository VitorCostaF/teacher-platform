package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String ATTEMPTS_KEY = "login:attempts:";
    private static final String LOCK_KEY = "login:lock:";
    private static final String RATE_KEY = "rate:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.auth.max-attempts}")
    private int maxAttempts;

    @Value("${app.auth.lockout-minutes}")
    private long lockoutMinutes;

    @Value("${app.auth.rate-limit-per-minute}")
    private long rateLimitPerMinute;

    public void checkRateLimit(String ip) {
        String key = RATE_KEY + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > rateLimitPerMinute) {
            throw new TooManyRequestsException();
        }
    }

    public long incrementAttempts(String email) {
        String key = ATTEMPTS_KEY + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(lockoutMinutes));
        }
        return count != null ? count : 1;
    }

    public Optional<Instant> getLockExpiry(String email) {
        Long ttl = redisTemplate.getExpire(LOCK_KEY + email, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            return Optional.empty();
        }
        return Optional.of(Instant.now().plusSeconds(ttl));
    }

    public void lockAccount(String email) {
        redisTemplate.opsForValue().set(LOCK_KEY + email, "1", Duration.ofMinutes(lockoutMinutes));
    }

    public void clearAttempts(String email) {
        redisTemplate.delete(ATTEMPTS_KEY + email);
        redisTemplate.delete(LOCK_KEY + email);
    }
}
