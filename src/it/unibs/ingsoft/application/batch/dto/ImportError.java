package it.unibs.ingsoft.application.batch.dto;

import it.unibs.ingsoft.application.batch.ImportFailure;

import java.util.Objects;

public record ImportError(ImportFailure failure) {
    public ImportError {
        failure = Objects.requireNonNull(failure);
    }
}
