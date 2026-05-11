package it.unibs.ingsoft.domain.shared.error;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public record ValidationError(
        DomainErrorCode code,
        String fieldName,
        List<String> details) {

    public ValidationError {
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ValidationError error(String fieldName, DomainErrorCode code) {
        return new ValidationError(code, fieldName, List.of());
    }

    public static ValidationError error(String fieldName, DomainErrorCode code, Object... details) {
        return new ValidationError(code, fieldName, details(details));
    }

    public static ValidationError termineIscrizioneNonFuturo(String fieldName, LocalDate oggi) {
        return error(fieldName, DomainErrorCode.TERMINE_ISCRIZIONE_NON_FUTURO, oggi);
    }

    public String detail(int index) {
        return index >= 0 && index < details.size() ? details.get(index) : null;
    }

    private static List<String> details(Object... values) {
        return Arrays.stream(values)
                .map(String::valueOf)
                .toList();
    }
}
