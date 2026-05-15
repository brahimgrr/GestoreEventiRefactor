package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.application.error.ApplicationFailure;

public sealed interface AuthenticationFailure extends ApplicationFailure
        permits AuthenticationFailure.UsernameInvalid,
        AuthenticationFailure.PasswordInvalid,
        AuthenticationFailure.UsernameTooShort,
        AuthenticationFailure.PasswordTooShort,
        AuthenticationFailure.UsernameReserved,
        AuthenticationFailure.UsernameAlreadyInUse {

    record UsernameInvalid() implements AuthenticationFailure {
    }

    record PasswordInvalid() implements AuthenticationFailure {
    }

    record UsernameTooShort(int minLength) implements AuthenticationFailure {
    }

    record PasswordTooShort(int minLength) implements AuthenticationFailure {
    }

    record UsernameReserved(String username) implements AuthenticationFailure {
    }

    record UsernameAlreadyInUse(String username) implements AuthenticationFailure {
    }
}
