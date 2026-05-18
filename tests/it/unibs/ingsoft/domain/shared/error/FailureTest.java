package it.unibs.ingsoft.domain.shared.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FailureTest {
    @Test
    void code_restituisceNomeClasseSemplice() {
        assertEquals("TestFailure", new TestFailure().code());
    }

    private record TestFailure() implements Failure {
    }
}
