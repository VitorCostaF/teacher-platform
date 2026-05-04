package br.com.inovadados.teacherplatform.dto.request;

import br.com.inovadados.teacherplatform.domain.enums.SistemaAvaliacaoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConfiguracoesEscolaRequest(
        @NotBlank String nome,
        @NotNull BigDecimal notaMinimaAprovacao,
        @NotNull BigDecimal frequenciaMinimaAprovacao,
        @NotNull SistemaAvaliacaoEnum sistemaAvaliacao
) {}
