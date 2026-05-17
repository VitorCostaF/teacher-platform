package br.com.inovadados.teacherplatform.exception;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Acesso negado a este recurso");
    }

    public AcessoNegadoException(String message) {
        super(message);
    }
}
