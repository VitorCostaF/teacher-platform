package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DesempenhoTurmaResponse(
        Long turmaId,
        String nomeTurma,
        String disciplina,
        BigDecimal media,
        BigDecimal maiorNota,
        BigDecimal menorNota,
        double percentualAprovacao,
        double percentualFrequencia,
        List<FaixaNotaDto> histograma,
        List<AlunoRankingDto> ranking,
        List<MediaHistoricaDto> linhaDoTempo
) {}
