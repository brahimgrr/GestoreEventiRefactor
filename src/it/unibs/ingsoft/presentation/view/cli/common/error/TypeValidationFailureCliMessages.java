package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;

public final class TypeValidationFailureCliMessages {
    private TypeValidationFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry
                .register(TypeValidationFailure.InvalidInteger.class, (failure, messages) ->
                        "Valore non valido: inserire un numero intero.")
                .register(TypeValidationFailure.InvalidPositiveInteger.class, (failure, messages) ->
                        "Valore non valido: inserire un numero intero positivo.")
                .register(TypeValidationFailure.InvalidDecimal.class, (failure, messages) ->
                        "Valore non valido: inserire un numero decimale.")
                .register(TypeValidationFailure.InvalidDate.class, (failure, messages) ->
                        "Valore non valido: inserire una data nel formato "
                                + AppConstants.DATE_FORMAT_LABEL + " (es. 25/12/2026).")
                .register(TypeValidationFailure.InvalidTime.class, (failure, messages) ->
                        "Valore non valido: inserire una data nel formato hh:mm (es. 16:30).")
                .register(TypeValidationFailure.InvalidBoolean.class, (failure, messages) ->
                        "Valore non valido: inserire s/si/si oppure n/no.");
    }
}
