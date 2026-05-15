package it.unibs.ingsoft.domain.policy.tipodato.strategies;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategy;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;

import java.time.LocalDate;
import java.util.Optional;

public final class DataTipoDatoValidationStrategy implements TipoDatoValidationStrategy {
    @Override
    public Optional<ValidationError> valida(String value) {
        try {
            LocalDate.parse(value.trim(), AppConstants.DATE_FMT);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidDate()));
        }
    }
}
