package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AlterarStatusProfessorRequest(boolean ativo, @NotBlank String motivo) {}
