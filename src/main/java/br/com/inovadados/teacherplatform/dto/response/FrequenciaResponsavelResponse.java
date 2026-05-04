package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;

import java.time.LocalDate;
import java.util.List;

public record FrequenciaResponsavelResponse(
        double percentualPresenca,
        int totalAulas,
        int totalPresencas,
        int totalFaltas,
        List<DiaFrequenciaDto> calendario,
        List<FaltaDto> faltas
) {
    public record DiaFrequenciaDto(
            LocalDate data,
            StatusFrequenciaEnum status,
            String disciplina
    ) {}

    public record FaltaDto(
            LocalDate data,
            String disciplina
    ) {}
}
