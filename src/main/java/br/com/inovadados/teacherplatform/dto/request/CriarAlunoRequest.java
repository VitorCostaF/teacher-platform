package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CriarAlunoRequest(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotEmpty List<Long> turmasIds,
        List<ResponsavelDto> responsaveis
) {
    public record ResponsavelDto(
            @NotBlank String nome,
            @Email @NotBlank String email,
            String parentesco
    ) {}
}
