package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;

public record GabaritoQuestaoDto(
        Long questaoId,
        Integer gabaritoIndice,
        String gabaritoDissertativo,
        Object respostaAluno,
        boolean correta,
        BigDecimal pontosObtidos,
        BigDecimal pontosTotal
) {}
