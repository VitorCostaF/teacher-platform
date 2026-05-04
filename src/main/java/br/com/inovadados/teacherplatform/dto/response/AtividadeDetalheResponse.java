package br.com.inovadados.teacherplatform.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AtividadeDetalheResponse(
        Long id,
        String titulo,
        String disciplina,
        String tipo,
        OffsetDateTime prazo,
        boolean permiteAtraso,
        boolean gabaritoDisponivel,
        String statusAluno,
        List<QuestaoAtividadeDto> questoes,
        Map<Long, Object> respostasRascunho
) {}
