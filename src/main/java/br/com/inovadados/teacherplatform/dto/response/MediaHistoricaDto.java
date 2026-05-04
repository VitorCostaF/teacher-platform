package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MediaHistoricaDto(
        Long avaliacaoId,
        String tituloAvaliacao,
        OffsetDateTime data,
        BigDecimal media
) {}
