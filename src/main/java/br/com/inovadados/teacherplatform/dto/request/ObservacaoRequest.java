package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ObservacaoRequest(@NotBlank String texto) {}
