package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.application.authentication.AuthenticationFailure;

public final class AuthenticationFailureCliMessages {
    private AuthenticationFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry
                .register(AuthenticationFailure.UsernameInvalid.class, (failure, messages) ->
                        "Username non valido.")
                .register(AuthenticationFailure.PasswordInvalid.class, (failure, messages) ->
                        "Password non valida.")
                .register(AuthenticationFailure.UsernameTooShort.class, (failure, messages) ->
                        "Username troppo corto (minimo " + failure.minLength() + " caratteri).")
                .register(AuthenticationFailure.PasswordTooShort.class, (failure, messages) ->
                        "Password troppo corta (minimo " + failure.minLength() + " caratteri).")
                .register(AuthenticationFailure.UsernameReserved.class, (failure, messages) ->
                        "Username riservato. Scegli un nome diverso.")
                .register(AuthenticationFailure.UsernameAlreadyInUse.class, (failure, messages) ->
                        "Username gia in uso. Scegli un nome diverso.");
    }
}
