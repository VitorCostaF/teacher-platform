package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GerarFlashcardsRequest(
        @NotBlank String disciplina,
        @NotBlank String serie,
        @Positive int quantidade,
        String conteudoTexto
) {}
