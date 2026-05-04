package br.com.inovadados.teacherplatform.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ObservacaoResponse(
        Long id,
        UUID alunoId,
        UUID professorId,
        String texto,
        OffsetDateTime criadoEm
) {}
