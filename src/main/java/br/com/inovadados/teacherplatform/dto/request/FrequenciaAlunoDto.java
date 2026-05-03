package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FrequenciaAlunoDto(
        @NotNull UUID alunoId,
        @NotNull StatusFrequenciaEnum status,
        String observacao
) {}
