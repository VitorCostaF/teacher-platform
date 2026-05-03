package br.com.inovadados.teacherplatform.dto.response;

public record GradeAulaDto(
        int semana,
        int aula,
        String conteudo,
        String objetivos,
        String recursosSugeridos
) {}
