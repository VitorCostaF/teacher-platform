package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminDashboardResponse(
        long totalAlunos,
        long totalProfessores,
        long totalTurmas,
        BigDecimal mediaNotas,
        BigDecimal frequenciaMedia,
        List<DesempenhoSerieDto> desempenhoPorSerie,
        List<AlertaDashboardDto> alertasConsolidados,
        List<AtividadeRecenteDto> atividadeRecente
) {
    public record DesempenhoSerieDto(String serie, BigDecimal media, int totalAlunos) {}

    public record AtividadeRecenteDto(String acao, String entidade, String entidadeId, OffsetDateTime criadoEm) {}
}
