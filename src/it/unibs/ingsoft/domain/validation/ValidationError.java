package it.unibs.ingsoft.domain.validation;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

public record ValidationError(
        String fieldName,
        ValidationErrorCode code,
        ValidationSeverity severity,
        Map<ValidationErrorParameter, String> parameters) {

    public ValidationError {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public static ValidationError error(String fieldName, ValidationErrorCode code) {
        return new ValidationError(fieldName, code, ValidationSeverity.ERROR, Map.of());
    }

    public static ValidationError termineIscrizioneNonFuturo(String fieldName, LocalDate oggi) {
        Map<ValidationErrorParameter, String> params = new EnumMap<>(ValidationErrorParameter.class);
        params.put(ValidationErrorParameter.OGGI, String.valueOf(oggi));
        return new ValidationError(
                fieldName,
                ValidationErrorCode.TERMINE_ISCRIZIONE_NON_FUTURO,
                ValidationSeverity.ERROR,
                params);
    }

    public String parameter(ValidationErrorParameter parameter) {
        return parameters.get(parameter);
    }
}
