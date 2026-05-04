package br.com.inovadados.teacherplatform.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PainelResponsavelResponse(
        AlunoResumoDto aluno,
        BigDecimal mediaGeral,
        double percentualFrequencia,
        AvaliacaoProximaDto proximaProva,
        List<AlertaDto> alertasAtivos
) {
    public record AlunoResumoDto(UUID id, String nome, String avatarUrl) {}

    public record AvaliacaoProximaDto(
            Long id,
            String titulo,
            String disciplina,
            OffsetDateTime disponivelEm
    ) {}

    public record AlertaDto(String tipo, String descricao) {}
}
