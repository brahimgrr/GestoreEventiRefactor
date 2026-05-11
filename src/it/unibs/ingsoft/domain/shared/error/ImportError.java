package it.unibs.ingsoft.domain.shared.error;

import java.util.Arrays;
import java.util.List;

public record ImportError(
        DomainErrorCode code,
        List<String> details,
        DomainException domainException,
        ValidationError validationError) {

    public ImportError {
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ImportError of(DomainErrorCode code, Object... details) {
        return new ImportError(code, details(details), null, null);
    }

    public static ImportError withDomainError(DomainErrorCode code, DomainException domainException, Object... details) {
        return new ImportError(code, details(details), domainException, null);
    }

    public static ImportError withValidationError(DomainErrorCode code, ValidationError validationError, Object... details) {
        return new ImportError(code, details(details), null, validationError);
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
