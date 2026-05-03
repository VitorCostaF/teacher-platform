package br.com.inovadados.teacherplatform.exception;

public class TurmaNaoEncontradaException extends RuntimeException {
    public TurmaNaoEncontradaException(Long id) {
        super("Turma não encontrada: " + id);
    }
}
