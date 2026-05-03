package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GerarGradeRequest(
        @NotBlank String disciplina,
        @NotBlank String serie,
        @Positive int totalAulas,
        List<String> topicos
) {}
