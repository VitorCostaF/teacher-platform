package br.com.inovadados.teacherplatform.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record SessaoProvaResponse(
        Long sessaoId,
        OffsetDateTime iniciadaEm,
        int duracaoMinutos,
        Map<Long, Object> respostasParciais
) {}
