package br.com.inovadados.teacherplatform.exception;

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException() {
        super("Muitas requisições. Tente novamente mais tarde");
    }
}
