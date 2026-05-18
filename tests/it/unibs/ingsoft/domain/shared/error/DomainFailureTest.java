package it.unibs.ingsoft.domain.shared.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainFailureTest {
    @Test
    void domainFailure_eAncheFailure() {
        DomainFailure failure = new TestDomainFailure();

        assertAll(
                () -> assertInstanceOf(Failure.class, failure),
                () -> assertEquals("TestDomainFailure", failure.code())
        );
    }

    private record TestDomainFailure() implements DomainFailure {
    }
}
