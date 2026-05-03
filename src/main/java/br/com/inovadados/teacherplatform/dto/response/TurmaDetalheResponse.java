package br.com.inovadados.teacherplatform.dto.response;

public record TurmaDetalheResponse(
        Long id,
        String nome,
        String disciplina,
        String serie,
        int totalAlunos,
        String proximaAula,
        String gradeHoraria,
        String periodoLetivo,
        String professor,
        TurmaResumoResponse.PendenciasDto pendencias
) {}
