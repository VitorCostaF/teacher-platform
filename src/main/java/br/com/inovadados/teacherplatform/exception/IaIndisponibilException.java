package br.com.inovadados.teacherplatform.exception;

public class IaIndisponibilException extends RuntimeException {
    public IaIndisponibilException() {
        super("Serviço de IA temporariamente indisponível. Tente novamente em instantes.");
    }
}
