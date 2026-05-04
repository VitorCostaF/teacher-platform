package br.com.inovadados.teacherplatform.dto.response;

public record AnaliseTopicoDto(
        String topico,
        int acertos,
        int total
) {}
