package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.domain.StatoProposta;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class DomainException extends IllegalStateException {
    private final DomainErrorCode code;
    private final List<String> details;

    public DomainException(DomainErrorCode code) {
        this(code, List.of());
    }

    public DomainException(DomainErrorCode code, Object... details) {
        this(code, details(details));
    }

    private DomainException(DomainErrorCode code, List<String> details) {
        super(code.name());
        this.code = code;
        this.details = List.copyOf(details);
    }

    public DomainErrorCode code() {
        return code;
    }

    public List<String> details() {
        return details;
    }

    public String detail(int index) {
        return index >= 0 && index < details.size() ? details.get(index) : null;
    }

    public String detail(int index, String defaultValue) {
        String value = detail(index);
        return value == null ? defaultValue : value;
    }

    public static DomainException invalidStateTransition(StatoProposta from, StatoProposta to) {
        return new DomainException(DomainErrorCode.INVALID_STATE_TRANSITION, from, to);
    }

    public static DomainException publicationDeadlineExpired(LocalDate termine) {
        return new DomainException(DomainErrorCode.PROPOSTA_PUBLICATION_DEADLINE_EXPIRED, termine);
    }

    public static DomainException subscriptionDeadlineExpired(LocalDate termine) {
        return new DomainException(DomainErrorCode.PROPOSTA_SUBSCRIPTION_DEADLINE_EXPIRED, termine);
    }

    public static DomainException unsubscriptionDeadlineExpired(LocalDate termine) {
        return new DomainException(DomainErrorCode.PROPOSTA_UNSUBSCRIPTION_DEADLINE_EXPIRED, termine);
    }

    public static DomainException fieldsNotModifiable(StatoProposta stato) {
        return new DomainException(DomainErrorCode.PROPOSTA_FIELDS_NOT_MODIFIABLE, stato);
    }

    public static DomainException participantsNotInteger(String value) {
        return new DomainException(DomainErrorCode.PROPOSTA_PARTICIPANTS_NOT_INTEGER, value);
    }

    private static List<String> details(Object... values) {
        return Arrays.stream(values)
                .map(String::valueOf)
                .toList();
    }
}
