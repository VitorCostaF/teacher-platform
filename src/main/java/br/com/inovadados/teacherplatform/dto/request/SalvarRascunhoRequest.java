package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.domain.enums.TipoAvaliacaoEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SalvarRascunhoRequest(
        @NotBlank String titulo,
        @NotNull TipoAvaliacaoEnum tipo,
        @NotNull Long turmaId,
        Integer duracaoMinutos,
        @Valid List<QuestaoRascunhoDto> questoes
) {}
