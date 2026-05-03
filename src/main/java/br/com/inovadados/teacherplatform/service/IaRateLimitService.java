package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.exception.IaRateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IaRateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.ia.rate-limit-por-hora:20}")
    private int limitePorHora;

    private String chave(UUID professorId) {
        return "ia:rate:" + professorId + ":hora";
    }

    public void verificarLimite(UUID professorId) {
        String valor = redisTemplate.opsForValue().get(chave(professorId));
        if (valor != null && Integer.parseInt(valor) >= limitePorHora) {
            throw new IaRateLimitException();
        }
    }

    public void incrementar(UUID professorId) {
        String key = chave(professorId);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            LocalTime now = LocalTime.now();
            long ttl = 3600L - (now.getMinute() * 60L + now.getSecond());
            redisTemplate.expire(key, ttl > 0 ? ttl : 1, TimeUnit.SECONDS);
        }
    }

    public void registrarUso(UUID professorId, Long escolaId, String endpoint, int tokensUsados) {
        jdbcTemplate.update(
                "INSERT INTO logs_uso_ia(professor_id, escola_id, endpoint, tokens_usados, criado_em) VALUES (?,?,?,?,NOW())",
                professorId, escolaId, endpoint, tokensUsados
        );
    }
}
