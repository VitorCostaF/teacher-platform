package br.com.inovadados.teacherplatform.dto.response;

import java.time.OffsetDateTime;

public record ItemFeedDto(
        Long id,
        String tipo,
        String titulo,
        String disciplina,
        OffsetDateTime prazo,
        String status,
        boolean atrasado
) {}
