package it.unibs.ingsoft.domain.error;

import java.util.Objects;

public record ValidationError(String fieldName, DomainFailure failure) {
    public ValidationError {
        failure = Objects.requireNonNull(failure);
    }
}
