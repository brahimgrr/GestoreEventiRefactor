package it.unibs.ingsoft.domain.shared.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfrastructureFailureTest {
    @Test
    void infrastructureFailure_eAncheFailure() {
        InfrastructureFailure failure = new TestInfrastructureFailure();

        assertAll(
                () -> assertInstanceOf(Failure.class, failure),
                () -> assertEquals("TestInfrastructureFailure", failure.code())
        );
    }

    private record TestInfrastructureFailure() implements InfrastructureFailure {
    }
}
