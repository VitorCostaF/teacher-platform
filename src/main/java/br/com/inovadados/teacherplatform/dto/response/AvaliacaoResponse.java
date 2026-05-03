package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoAvaliacaoEnum;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AvaliacaoResponse(
        Long id,
        String titulo,
        TipoAvaliacaoEnum tipo,
        StatusAvaliacaoEnum status,
        Integer duracaoMinutos,
        OffsetDateTime disponivelEm,
        OffsetDateTime encerraEm,
        boolean embaralharQuestoes,
        boolean embaralharAlternativas,
        GabaritoLiberacaoEnum gabaritoLiberacao,
        BigDecimal pesoNota,
        boolean geradoPorIa,
        OffsetDateTime criadoEm,
        List<QuestaoAvaliacaoDto> questoes
) {}
