package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EvolucaoNotaDto(
        Long avaliacaoId,
        String tituloAvaliacao,
        OffsetDateTime data,
        BigDecimal nota,
        int posicaoNaTurma,
        int totalAlunos
) {}
