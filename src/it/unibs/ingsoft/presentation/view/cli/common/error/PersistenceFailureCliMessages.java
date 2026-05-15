package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.persistence.error.PersistenceFailure;

public final class PersistenceFailureCliMessages {
    private PersistenceFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry
                .register(PersistenceFailure.ReadFailed.class, (failure, messages) ->
                        "Impossibile leggere i dati da: " + failure.path())
                .register(PersistenceFailure.WriteFailed.class, (failure, messages) ->
                        "Impossibile salvare i dati in: " + failure.path());
    }
}
