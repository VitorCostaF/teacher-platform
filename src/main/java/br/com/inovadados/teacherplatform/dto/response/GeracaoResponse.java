package br.com.inovadados.teacherplatform.dto.response;

import java.util.List;

public record GeracaoResponse(List<QuestaoGeradaDto> questoes, int tokensUsados) {}
