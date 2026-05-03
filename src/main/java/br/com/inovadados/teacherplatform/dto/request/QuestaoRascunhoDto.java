package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record QuestaoRascunhoDto(
        Long id,
        @NotBlank String enunciado,
        @NotNull TipoQuestaoEnum tipo,
        List<String> alternativas,
        String gabaritoDissertativo,
        Integer gabaritoIndice,
        String dificuldade,
        String topico,
        BigDecimal pontos
) {}
