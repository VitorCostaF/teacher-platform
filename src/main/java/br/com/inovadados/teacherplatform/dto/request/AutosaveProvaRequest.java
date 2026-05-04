package br.com.inovadados.teacherplatform.dto.request;

import java.util.Map;

public record AutosaveProvaRequest(
        Map<Long, Object> respostas,
        String eventoVisibilidade
) {}
