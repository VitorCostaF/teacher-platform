package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;

public record DesempenhoDisciplinaDto(
        String disciplina,
        BigDecimal mediaNotas,
        String tendencia
) {}
