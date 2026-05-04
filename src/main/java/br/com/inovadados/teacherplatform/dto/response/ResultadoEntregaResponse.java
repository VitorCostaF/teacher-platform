package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ResultadoEntregaResponse(
        BigDecimal nota,
        BigDecimal mediaTurma,
        boolean gabaritoDisponivel,
        List<GabaritoQuestaoDto> gabarito,
        List<AnaliseTopicoDto> analiseTopicos
) {}
