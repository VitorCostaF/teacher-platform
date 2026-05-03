package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarTurmaRequest(
        @NotBlank String nome,
        @NotBlank String disciplina,
        @NotBlank String serie,
        @NotNull Long periodoLetivoId,
        @NotNull UUID professorId,
        String gradeHoraria
) {}
