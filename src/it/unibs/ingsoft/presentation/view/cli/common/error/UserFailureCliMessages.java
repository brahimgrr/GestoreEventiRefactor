package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.utente.UserFailure;

public final class UserFailureCliMessages {
    private UserFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry.register(UserFailure.UsernameInvalid.class, (failure, messages) ->
                "Lo username non puo essere vuoto.");
    }
}
