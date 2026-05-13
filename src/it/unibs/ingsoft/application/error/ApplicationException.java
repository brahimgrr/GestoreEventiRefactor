package it.unibs.ingsoft.application.error;

import it.unibs.ingsoft.domain.shared.error.ApplicationFailure;
import it.unibs.ingsoft.domain.shared.error.FailureException;

public class ApplicationException extends FailureException {
    public ApplicationException(ApplicationFailure failure) {
        super(failure);
    }

    public ApplicationException(ApplicationFailure failure, Throwable cause) {
        super(failure, cause);
    }

    @Override
    public ApplicationFailure failure() {
        return (ApplicationFailure) super.failure();
    }
}
