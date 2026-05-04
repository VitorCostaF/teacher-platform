package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CalendarioResponsavelResponse(
        List<ProvaFuturaDto> proximasProvas,
        List<ProvaHistoricoDto> historicoAvaliacoes
) {
    public record ProvaFuturaDto(
            Long id,
            String titulo,
            String disciplina,
            String tipo,
            OffsetDateTime disponivelEm,
            OffsetDateTime encerraEm
    ) {}

    public record ProvaHistoricoDto(
            Long id,
            String titulo,
            String disciplina,
            String tipo,
            OffsetDateTime disponivelEm,
            BigDecimal nota
    ) {}
}
