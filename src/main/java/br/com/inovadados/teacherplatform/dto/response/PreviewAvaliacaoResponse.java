package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.domain.enums.TipoAvaliacaoEnum;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PreviewAvaliacaoResponse(
        Long id,
        String titulo,
        TipoAvaliacaoEnum tipo,
        Integer duracaoMinutos,
        OffsetDateTime disponivelEm,
        BigDecimal pesoNota,
        List<QuestaoAvaliacaoDto> questoes
) {}
