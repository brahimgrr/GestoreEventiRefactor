package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.domain.shared.error.ApplicationFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
Non ha senso testare interfaccia. Tolgo?
 */
class AuthenticationFailure_Test {
    @Test
    void tutteLeFailure_sonoApplicationFailure() {
        assertAll(
                () -> assertInstanceOf(ApplicationFailure.class, new AuthenticationFailure.UsernameInvalid()),
                () -> assertInstanceOf(ApplicationFailure.class, new AuthenticationFailure.PasswordInvalid()),
                () -> assertInstanceOf(ApplicationFailure.class, new AuthenticationFailure.UsernameTooShort(3)),
                () -> assertInstanceOf(ApplicationFailure.class, new AuthenticationFailure.PasswordTooShort(4)),
                () -> assertInstanceOf(ApplicationFailure.class, new AuthenticationFailure.UsernameReserved("config")),
                () -> assertInstanceOf(ApplicationFailure.class, new AuthenticationFailure.UsernameAlreadyInUse("mario"))
        );
    }

    @Test
    void failureConCampi_conservanoValori() {
        assertAll(
                () -> assertEquals(3, new AuthenticationFailure.UsernameTooShort(3).minLength()),
                () -> assertEquals(4, new AuthenticationFailure.PasswordTooShort(4).minLength()),
                () -> assertEquals("config", new AuthenticationFailure.UsernameReserved("config").username()),
                () -> assertEquals("mario", new AuthenticationFailure.UsernameAlreadyInUse("mario").username())
        );
    }

    @Test
    void record_siComportanoComeValueObject() {
        AuthenticationFailure.UsernameReserved first = new AuthenticationFailure.UsernameReserved("config");
        AuthenticationFailure.UsernameReserved second = new AuthenticationFailure.UsernameReserved("config");

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertTrue(first.toString().contains("config"))
        );
    }
}
