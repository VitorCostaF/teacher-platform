package br.com.inovadados.teacherplatform.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record AlunoTurmaResponse(
        UUID id,
        String nome,
        String email,
        String avatarUrl,
        LocalDate matriculadoEm
) {}
