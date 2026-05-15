package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.error.DomainFailure;
import it.unibs.ingsoft.shared.error.FailureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FailureExceptionTest {
    @Test
    void failureException_restaRuntimeExceptionSenzaEssereIllegalStateException() {
        FailureException exception = new FailureException(new TestFailure());

        assertTrue(exception instanceof RuntimeException);
        assertFalse(((Object) exception) instanceof IllegalStateException);
    }

    private record TestFailure() implements DomainFailure {
    }
}
