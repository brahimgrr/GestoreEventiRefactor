package it.unibs.ingsoft.domain.policy.tipodato.strategies;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategy;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;

import java.time.LocalTime;
import java.util.Optional;

public final class OraTipoDatoValidationStrategy implements TipoDatoValidationStrategy {
    @Override
    public Optional<ValidationError> valida(String value) {
        try {
            LocalTime.parse(value.trim(), AppConstants.TIME_FMT);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidTime()));
        }
    }
}
