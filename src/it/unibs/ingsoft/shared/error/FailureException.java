package it.unibs.ingsoft.shared.error;

import java.util.Objects;

public class FailureException extends RuntimeException {
    private final Failure failure;

    public FailureException(Failure failure) {
        super(Objects.requireNonNull(failure).getClass().getName());
        this.failure = failure;
    }

    public FailureException(Failure failure, Throwable cause) {
        super(Objects.requireNonNull(failure).getClass().getName(), cause);
        this.failure = failure;
    }

    public Failure failure() {
        return failure;
    }
}
