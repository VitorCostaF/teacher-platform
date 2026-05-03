package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.dto.response.QuestaoGeradaDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegerarQuestaoRequest(
        @NotBlank String contextoProva,
        @NotNull QuestaoGeradaDto questaoAtual
) {}
