package br.com.inovadados.teacherplatform.exception;

import java.time.Instant;

public class AccountLockedException extends RuntimeException {

    private final Instant desbloqueiaEm;

    public AccountLockedException(Instant desbloqueiaEm) {
        super("Conta bloqueada por múltiplas tentativas inválidas");
        this.desbloqueiaEm = desbloqueiaEm;
    }

    public Instant getDesbloqueiaEm() {
        return desbloqueiaEm;
    }
}
