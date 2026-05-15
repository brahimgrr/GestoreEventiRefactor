package it.unibs.ingsoft.domain.policy.tipodato;

import it.unibs.ingsoft.domain.error.DomainFailure;

public sealed interface TypeValidationFailure extends DomainFailure
        permits TypeValidationFailure.InvalidInteger,
        TypeValidationFailure.InvalidPositiveInteger,
        TypeValidationFailure.InvalidDecimal,
        TypeValidationFailure.InvalidDate,
        TypeValidationFailure.InvalidTime,
        TypeValidationFailure.InvalidBoolean {

    record InvalidInteger() implements TypeValidationFailure {
    }

    record InvalidPositiveInteger() implements TypeValidationFailure {
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
