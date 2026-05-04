package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AlunoRankingDto(
        int posicao,
        UUID alunoId,
        String nomeAluno,
        BigDecimal media,
        String tendencia
) {}
