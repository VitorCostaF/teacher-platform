package br.com.inovadados.teacherplatform.dto.response;

public record RelatorioStatusResponse(
        String jobId,
        String status,
        String urlDownload
) {}
