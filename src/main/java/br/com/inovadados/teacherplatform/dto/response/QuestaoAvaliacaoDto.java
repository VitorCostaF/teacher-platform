package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;

import java.math.BigDecimal;
import java.util.List;

public record QuestaoAvaliacaoDto(
        Long id,
        int ordem,
        TipoQuestaoEnum tipo,
        String enunciado,
        List<String> alternativas,
        String dificuldade,
        String topico,
        BigDecimal pontos,
        String gabaritoDissertativo,
        Integer gabaritoIndice
) {}
