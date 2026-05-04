package br.com.inovadados.teacherplatform.dto.response;

import java.time.OffsetDateTime;

public record ConteudoFeedDto(
        Long id,
        String titulo,
        String tipo,
        String disciplina,
        OffsetDateTime publicadoEm
) {}
