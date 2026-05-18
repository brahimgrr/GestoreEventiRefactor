package it.unibs.ingsoft.domain.shared.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
TUTTO il pacchetto non ha senso di essere testato dato che sono tutte
interfacce tranne ValidationError che è un record ma non fa nulla di che
 */
class ApplicationFailure_Test {
    @Test
    void applicationFailure_eAncheFailure() {
        ApplicationFailure failure = new TestApplicationFailure();

        assertAll(
                () -> assertInstanceOf(Failure.class, failure),
                () -> assertEquals("TestApplicationFailure", failure.code())
        );
    }

    private record TestApplicationFailure() implements ApplicationFailure {
    }
}
