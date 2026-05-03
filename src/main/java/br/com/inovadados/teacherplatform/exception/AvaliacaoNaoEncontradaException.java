package br.com.inovadados.teacherplatform.exception;

public class AvaliacaoNaoEncontradaException extends RuntimeException {
    public AvaliacaoNaoEncontradaException(Long id) {
        super("Avaliação não encontrada: " + id);
    }
}
