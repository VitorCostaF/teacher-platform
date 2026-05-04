package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;

public record TurmaDashboardDto(
        Long id,
        String nome,
        String disciplina,
        int totalAlunos,
        BigDecimal mediaNotas,
        BigDecimal percentualPresenca,
        int avaliacoesPendentesCorrecao
) {}
