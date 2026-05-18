package it.unibs.ingsoft.application.error;

import it.unibs.ingsoft.application.authentication.AuthenticationFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
Classe di errore. ha senso testare?
 */
class ApplicationException_Test {
    @Test
    void costruttore_conFailure_impostaMessaggioEFailureTipizzata() {
        AuthenticationFailure.PasswordInvalid failure = new AuthenticationFailure.PasswordInvalid();

        ApplicationException exception = new ApplicationException(failure);

        assertAll(
                () -> assertSame(failure, exception.failure()),
                () -> assertEquals(failure.getClass().getName(), exception.getMessage())
        );
    }

    @Test
    void costruttore_conCause_preservaCauseEFailure() {
        AuthenticationFailure.UsernameInvalid failure = new AuthenticationFailure.UsernameInvalid();
        RuntimeException cause = new RuntimeException("boom");

        ApplicationException exception = new ApplicationException(failure, cause);

        assertAll(
                () -> assertSame(failure, exception.failure()),
                () -> assertSame(cause, exception.getCause())
        );
    }

    @Test
    void costruttori_conFailureNull_lancianoNullPointerException() {
        RuntimeException cause = new RuntimeException("boom");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new ApplicationException(null)),
                () -> assertThrows(NullPointerException.class, () -> new ApplicationException(null, cause))
        );
    }
}
