package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DesempenhoAlunoResponse(
        UUID alunoId,
        String nomeAluno,
        String situacao,
        BigDecimal mediaGeral,
        double percentualFrequencia,
        List<EvolucaoNotaDto> evolucaoNotas,
        List<FrequenciaMensalDto> frequenciaMensal,
        List<TopicoAcertoDto> topicos
) {}
