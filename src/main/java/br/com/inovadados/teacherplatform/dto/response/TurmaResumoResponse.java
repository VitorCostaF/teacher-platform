package br.com.inovadados.teacherplatform.dto.response;

public record TurmaResumoResponse(
        Long id,
        String nome,
        String disciplina,
        int totalAlunos,
        String proximaAula,
        PendenciasDto pendencias
) {
    public record PendenciasDto(int frequenciasNaoLancadas, int atividadesNaoCorrigidas) {}
}
