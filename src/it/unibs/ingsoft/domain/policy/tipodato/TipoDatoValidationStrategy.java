package it.unibs.ingsoft.domain.policy.tipodato;

import it.unibs.ingsoft.domain.error.ValidationError;

import java.util.Optional;

@FunctionalInterface
public interface TipoDatoValidationStrategy {
    Optional<ValidationError> valida(String value);
}
