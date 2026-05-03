package br.com.inovadados.teacherplatform.dto.response;

import java.util.List;

public record ImportacaoResponse(int importados, List<ErroImportacao> erros) {
    public record ErroImportacao(int linha, String motivo) {}
}
