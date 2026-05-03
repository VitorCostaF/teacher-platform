package br.com.inovadados.teacherplatform.exception;

public class IaRateLimitException extends RuntimeException {
    public IaRateLimitException() {
        super("Limite de requisições de IA atingido. Tente novamente na próxima hora.");
    }
}
