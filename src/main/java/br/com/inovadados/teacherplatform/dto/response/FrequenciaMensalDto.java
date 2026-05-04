package br.com.inovadados.teacherplatform.dto.response;

public record FrequenciaMensalDto(
        int ano,
        int mes,
        int totalAulas,
        int presencas,
        double percentual
) {}
