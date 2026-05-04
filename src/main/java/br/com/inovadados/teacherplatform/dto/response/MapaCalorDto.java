package br.com.inovadados.teacherplatform.dto.response;

import java.time.LocalDate;

public record MapaCalorDto(
        LocalDate data,
        Long turmaId,
        int totalAlunos,
        int presentes,
        double percentualPresenca
) {}
