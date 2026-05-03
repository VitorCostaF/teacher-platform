package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;

import java.util.List;

public record QuestaoGeradaDto(
        String id,
        TipoQuestaoEnum tipo,
        String enunciado,
        List<String> alternativas,
        Integer gabarito,
        String dificuldade,
        String topico,
        String criteriosCorrecao
) {}
