package it.unibs.ingsoft.domain.shared.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FailureExceptionTest {
    @Test
    void failureException_restaRuntimeExceptionSenzaEssereIllegalStateException() {
        TestFailure failure = new TestFailure();
        FailureException exception = new FailureException(failure);

        assertAll(
                () -> assertTrue(exception instanceof RuntimeException),
                () -> assertFalse(((Object) exception) instanceof IllegalStateException),
                () -> assertSame(failure, exception.failure()),
                () -> assertEquals(failure.getClass().getName(), exception.getMessage())
        );
    }

    @Test
    void costruttoreConCause_preservaFailureECause() {
        TestFailure failure = new TestFailure();
        RuntimeException cause = new RuntimeException("boom");
        FailureException exception = new FailureException(failure, cause);

        assertAll(
                () -> assertSame(failure, exception.failure()),
                () -> assertSame(cause, exception.getCause()),
                () -> assertEquals(failure.getClass().getName(), exception.getMessage())
        );
    }

    @Test
    void costruttori_conFailureNull_lancianoNullPointerException() {
        RuntimeException cause = new RuntimeException("boom");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new FailureException(null)),
                () -> assertThrows(NullPointerException.class, () -> new FailureException(null, cause))
        );
    }

    private record TestFailure() implements DomainFailure {
    }
}
