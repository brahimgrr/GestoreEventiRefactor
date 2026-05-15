package it.unibs.ingsoft.domain.policy.tipodato.strategies;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategy;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;

import java.util.Optional;

public final class InteroTipoDatoValidationStrategy implements TipoDatoValidationStrategy {
    @Override
    public Optional<ValidationError> valida(String value) {
        try {
            Integer.parseInt(value.trim());
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidInteger()));
        }
    }
}
