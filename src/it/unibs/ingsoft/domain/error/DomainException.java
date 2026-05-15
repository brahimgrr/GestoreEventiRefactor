package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.shared.error.FailureException;

public class DomainException extends FailureException {
    public DomainException(DomainFailure failure) {
        super(failure);
    }

    public DomainException(DomainFailure failure, Throwable cause) {
        super(failure, cause);
    }

    @Override
    public DomainFailure failure() {
        return (DomainFailure) super.failure();
    }
}
