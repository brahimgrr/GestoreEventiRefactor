package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.ValidationError;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

/**
 * Implementazione di default di {@link TypeValidator}.
 * Verifica che una stringa grezza sia parseable come il {@link TipoDato} atteso.
 */
public final class DefaultTypeValidator implements TypeValidator {
    public static final DefaultTypeValidator INSTANCE = new DefaultTypeValidator();

    private static final Set<String> VALORI_SI = Set.of("s", "si", "s\u00ec", "true");
    private static final Set<String> VALORI_NO = Set.of("n", "no", "false");

    private DefaultTypeValidator() {
    }

    @Override
    public Optional<ValidationError> validate(String input, TipoDato tipo) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        return switch (tipo) {
            case STRINGA -> Optional.empty();
            case INTERO -> validateIntero(input);
            case DECIMALE -> validateDecimale(input);
            case DATA -> validateData(input);
            case BOOLEANO -> validateBooleano(input);
            case ORA -> validateOra(input);
        };
    }

    private Optional<ValidationError> validateOra(String value) {
        try {
            LocalTime.parse(value.trim(), AppConstants.TIME_FMT);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidTime()));
        }
    }

    private Optional<ValidationError> validateIntero(String value) {
        try {
            Integer.parseInt(value.trim());
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidInteger()));
        }
    }

    private Optional<ValidationError> validateDecimale(String value) {
        try {
            Double.parseDouble(value.trim().replace(',', '.'));
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidDecimal()));
        }
    }

    private Optional<ValidationError> validateData(String value) {
        try {
            LocalDate.parse(value.trim(), AppConstants.DATE_FMT);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidDate()));
        }
    }

    private Optional<ValidationError> validateBooleano(String value) {
        String lower = value.trim().toLowerCase();
        if (VALORI_SI.contains(lower) || VALORI_NO.contains(lower)) {
            return Optional.empty();
        }
        return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidBoolean()));
    }
}
