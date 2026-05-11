package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import it.unibs.ingsoft.domain.shared.AppConstants;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Implementazione di default di {@link TypeValidator}.
 * Verifica che una stringa grezza sia parseable come il {@link TipoDato} atteso.
 */
public final class DefaultTypeValidator implements TypeValidator {
    public static final DefaultTypeValidator INSTANCE = new DefaultTypeValidator();

    private static final Set<String> VALORI_SI = Set.of("s", "si", "sì", "true");
    private static final Set<String> VALORI_NO = Set.of("n", "no", "false");

    private DefaultTypeValidator() {
    }

    @Override
    public ValidationError validate(String input, TipoDato tipo) {
        if (input == null || input.isBlank()) return null;

        switch (tipo) {
            case STRINGA:
                return null;
            case INTERO:
                return validateIntero(input);
            case DECIMALE:
                return validateDecimale(input);
            case DATA:
                return validateData(input);
            case BOOLEANO:
                return validateBooleano(input);
            case ORA:
                return validateOra(input);
            default:
                return null;
        }
    }

    private ValidationError validateOra(String s) {
        try {
            LocalTime.parse(s.trim(), AppConstants.TIME_FMT);
            return null;
        } catch (Exception e) {
            return ValidationError.error(null, DomainErrorCode.TIPO_ORA_NON_VALIDA);
        }
    }

    private ValidationError validateIntero(String s) {
        try {
            Integer.parseInt(s.trim());
            return null;
        } catch (NumberFormatException e) {
            return ValidationError.error(null, DomainErrorCode.TIPO_INTERO_NON_VALIDO);
        }
    }

    private ValidationError validateDecimale(String s) {
        try {
            Double.parseDouble(s.trim().replace(',', '.'));
            return null;
        } catch (NumberFormatException e) {
            return ValidationError.error(null, DomainErrorCode.TIPO_DECIMALE_NON_VALIDO);
        }
    }

    private ValidationError validateData(String s) {
        try {
            LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
            return null;
        } catch (Exception e) {
            return ValidationError.error(null, DomainErrorCode.TIPO_DATA_NON_VALIDA);
        }
    }

    private ValidationError validateBooleano(String s) {
        String lower = s.trim().toLowerCase();
        if (VALORI_SI.contains(lower) || VALORI_NO.contains(lower)) return null;
        return ValidationError.error(null, DomainErrorCode.TIPO_BOOLEANO_NON_VALIDO);
    }
}
