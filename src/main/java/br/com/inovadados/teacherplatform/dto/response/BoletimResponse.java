package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BoletimResponse(
        String periodo,
        List<BoletimDisciplinaDto> disciplinas
) {
    public record BoletimDisciplinaDto(
            String disciplina,
            BigDecimal notaBimestre1,
            BigDecimal notaBimestre2,
            BigDecimal notaBimestre3,
            BigDecimal notaBimestre4,
            BigDecimal mediaFinal,
            String situacao
    ) {}
}
