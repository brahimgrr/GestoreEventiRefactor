package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
Interfaccia non ha senso di essere testata
 */
class TypeValidationFailure_Test {
    @Test
    void tutteLeFailure_sonoDomainFailure() {
        assertAll(
                () -> assertInstanceOf(DomainFailure.class, new TypeValidationFailure.InvalidInteger()),
                () -> assertInstanceOf(DomainFailure.class, new TypeValidationFailure.InvalidDecimal()),
                () -> assertInstanceOf(DomainFailure.class, new TypeValidationFailure.InvalidDate()),
                () -> assertInstanceOf(DomainFailure.class, new TypeValidationFailure.InvalidTime()),
                () -> assertInstanceOf(DomainFailure.class, new TypeValidationFailure.InvalidBoolean())
        );
    }

    @Test
    void recordSenzaCampi_siComportanoComeValueObject() {
        TypeValidationFailure.InvalidInteger first = new TypeValidationFailure.InvalidInteger();
        TypeValidationFailure.InvalidInteger second = new TypeValidationFailure.InvalidInteger();

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertEquals("InvalidInteger[]", first.toString())
        );
    }
}
