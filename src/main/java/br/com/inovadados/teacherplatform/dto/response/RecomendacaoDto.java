package br.com.inovadados.teacherplatform.dto.response;

public record RecomendacaoDto(
        Long id,
        String tipo,
        String titulo,
        String disciplina,
        String motivo
) {}
