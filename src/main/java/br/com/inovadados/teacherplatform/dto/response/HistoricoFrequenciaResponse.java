package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;

import java.time.LocalDate;
import java.util.List;

public record HistoricoFrequenciaResponse(
        double percentualPresenca,
        int totalAulas,
        int totalPresencas,
        int totalFaltas,
        List<DiaFrequenciaDto> calendario
) {
    public record DiaFrequenciaDto(
            LocalDate data,
            StatusFrequenciaEnum status,
            String observacao
    ) {}
}
