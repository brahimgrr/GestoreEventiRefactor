package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.domain.StatoProposta;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

public class DomainException extends IllegalStateException {
    private final DomainErrorCode code;
    private final Map<DomainErrorParameter, String> parameters;

    public DomainException(DomainErrorCode code) {
        this(code, Map.of());
    }

    private DomainException(DomainErrorCode code, Map<DomainErrorParameter, String> parameters) {
        super(code.name());
        this.code = code;
        this.parameters = Map.copyOf(parameters);
    }

    public DomainErrorCode code() {
        return code;
    }

    public Map<DomainErrorParameter, String> parameters() {
        return parameters;
    }

    public String parameter(DomainErrorParameter parameter) {
        return parameters.get(parameter);
    }

    public String parameter(DomainErrorParameter parameter, String defaultValue) {
        return parameters.getOrDefault(parameter, defaultValue);
    }

    public static DomainException invalidStateTransition(StatoProposta from, StatoProposta to) {
        Map<DomainErrorParameter, String> params = new EnumMap<>(DomainErrorParameter.class);
        params.put(DomainErrorParameter.FROM, String.valueOf(from));
        params.put(DomainErrorParameter.TO, String.valueOf(to));
        return new DomainException(DomainErrorCode.INVALID_STATE_TRANSITION, params);
    }

    public static DomainException publicationDeadlineExpired(LocalDate termine) {
        return withParameter(DomainErrorCode.PROPOSTA_PUBLICATION_DEADLINE_EXPIRED,
                DomainErrorParameter.TERMINE, termine);
    }

    public static DomainException subscriptionDeadlineExpired(LocalDate termine) {
        return withParameter(DomainErrorCode.PROPOSTA_SUBSCRIPTION_DEADLINE_EXPIRED,
                DomainErrorParameter.TERMINE, termine);
    }

    public static DomainException unsubscriptionDeadlineExpired(LocalDate termine) {
        return withParameter(DomainErrorCode.PROPOSTA_UNSUBSCRIPTION_DEADLINE_EXPIRED,
                DomainErrorParameter.TERMINE, termine);
    }

    public static DomainException fieldsNotModifiable(StatoProposta stato) {
        return withParameter(DomainErrorCode.PROPOSTA_FIELDS_NOT_MODIFIABLE,
                DomainErrorParameter.STATO, stato);
    }

    public static DomainException participantsNotInteger(String value) {
        return withParameter(DomainErrorCode.PROPOSTA_PARTICIPANTS_NOT_INTEGER,
                DomainErrorParameter.VALUE, value);
    }

    private static DomainException withParameter(DomainErrorCode code,
                                                 DomainErrorParameter parameter,
                                                 Object value) {
        Map<DomainErrorParameter, String> params = new EnumMap<>(DomainErrorParameter.class);
        params.put(parameter, String.valueOf(value));
        return new DomainException(code, params);
    }
}
