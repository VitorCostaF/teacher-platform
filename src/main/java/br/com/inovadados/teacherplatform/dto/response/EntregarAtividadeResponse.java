package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record EntregarAtividadeResponse(
        Long entregaId,
        BigDecimal nota,
        boolean gabaritoDisponivel,
        List<GabaritoQuestaoDto> gabarito
) {}
