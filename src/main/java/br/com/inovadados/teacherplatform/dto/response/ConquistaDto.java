package br.com.inovadados.teacherplatform.dto.response;

import java.time.OffsetDateTime;

public record ConquistaDto(
        Long id,
        String tipo,
        String descricao,
        OffsetDateTime obtidaEm
) {}
