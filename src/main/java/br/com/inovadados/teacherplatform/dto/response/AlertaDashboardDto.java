package br.com.inovadados.teacherplatform.dto.response;

public record AlertaDashboardDto(
        String tipo,
        String descricao,
        Long referenciaId,
        String referenciaUrl
) {}
