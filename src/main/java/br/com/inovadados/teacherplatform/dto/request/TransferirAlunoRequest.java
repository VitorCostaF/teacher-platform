package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotNull;

public record TransferirAlunoRequest(@NotNull Long novaTurmaId) {}
