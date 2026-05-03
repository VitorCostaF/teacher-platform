package br.com.inovadados.teacherplatform.event;

import java.util.List;

public record AvaliacaoPublicadaEvent(Long avaliacaoId, List<Long> turmasIds) {}
