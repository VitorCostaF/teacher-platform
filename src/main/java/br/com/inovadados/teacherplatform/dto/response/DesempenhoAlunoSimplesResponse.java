package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DesempenhoAlunoSimplesResponse(
        BigDecimal mediaGeral,
        int totalEntregas,
        int entregasNoPrazo,
        List<DesempenhoDisciplinaDto> porDisciplina,
        List<ConquistaDto> conquistas
) {}
