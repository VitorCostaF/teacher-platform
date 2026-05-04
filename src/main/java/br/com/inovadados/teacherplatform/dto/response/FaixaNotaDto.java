package br.com.inovadados.teacherplatform.dto.response;

import java.util.List;

public record FaixaNotaDto(
        String faixa,
        int quantidade,
        List<String> nomesAlunos
) {}
