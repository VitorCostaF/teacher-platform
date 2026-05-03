package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.domain.enums.NivelDificuldadeEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record GerarProvaRequest(
        @NotBlank String disciplina,
        @NotBlank String serie,
        @NotNull NivelDificuldadeEnum dificuldade,
        Map<TipoQuestaoEnum, Integer> quantidadesPorTipo,
        String conteudoTexto,
        List<String> topicos
) {}
