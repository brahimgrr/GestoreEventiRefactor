package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;

public sealed interface TypeValidationFailure extends DomainFailure
        permits TypeValidationFailure.InvalidInteger,
        TypeValidationFailure.InvalidDecimal,
        TypeValidationFailure.InvalidDate,
        TypeValidationFailure.InvalidTime,
        TypeValidationFailure.InvalidBoolean {

    record InvalidInteger() implements TypeValidationFailure {
    }

    record InvalidDecimal() implements TypeValidationFailure {
    }

    record InvalidDate() implements TypeValidationFailure {
    }

    record InvalidTime() implements TypeValidationFailure {
    }

    record InvalidBoolean() implements TypeValidationFailure {
    }
}
