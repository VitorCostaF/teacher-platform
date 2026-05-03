package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PublicarAvaliacaoRequest(
        @NotNull OffsetDateTime disponivelEm,
        OffsetDateTime encerraEm,
        @NotEmpty List<Long> turmasIds,
        boolean embaralharQuestoes,
        boolean embaralharAlternativas,
        @NotNull GabaritoLiberacaoEnum gabaritoLiberacao,
        BigDecimal pesoNota
) {}
