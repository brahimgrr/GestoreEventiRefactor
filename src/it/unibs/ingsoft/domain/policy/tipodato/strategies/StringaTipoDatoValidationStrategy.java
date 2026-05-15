package it.unibs.ingsoft.domain.policy.tipodato.strategies;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategy;

import java.util.Optional;

public final class StringaTipoDatoValidationStrategy implements TipoDatoValidationStrategy {
    @Override
    public Optional<ValidationError> valida(String value) {
        return Optional.empty();
    }
}
