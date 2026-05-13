package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.shared.error.FailureException;
import it.unibs.ingsoft.domain.shared.error.InfrastructureFailure;

public class PersistenceException extends FailureException {
    public PersistenceException(InfrastructureFailure failure, Throwable cause) {
        super(failure, cause);
    }

    @Override
    public InfrastructureFailure failure() {
        return (InfrastructureFailure) super.failure();
    }
}
