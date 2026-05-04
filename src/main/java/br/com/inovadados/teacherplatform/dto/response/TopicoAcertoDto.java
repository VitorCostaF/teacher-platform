package br.com.inovadados.teacherplatform.dto.response;

public record TopicoAcertoDto(
        String topico,
        int acertos,
        int total,
        double percentual
) {}
