package it.unibs.ingsoft.persistence.error;

import it.unibs.ingsoft.shared.error.FailureException;

public class PersistenceException extends FailureException {
    public PersistenceException(InfrastructureFailure failure, Throwable cause) {
        super(failure, cause);
    }

    @Override
    public InfrastructureFailure failure() {
        return (InfrastructureFailure) super.failure();
    }
}
