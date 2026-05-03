package br.com.inovadados.teacherplatform.exception;

public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException() {
        super("Conta inativa");
    }
}
