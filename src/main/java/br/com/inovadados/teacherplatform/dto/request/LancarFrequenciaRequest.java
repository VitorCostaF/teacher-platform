package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record LancarFrequenciaRequest(
        @NotNull LocalDate data,
        @NotEmpty @Valid List<FrequenciaAlunoDto> alunos
) {}
