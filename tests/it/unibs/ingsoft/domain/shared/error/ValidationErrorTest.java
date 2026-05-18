package it.unibs.ingsoft.domain.shared.error;

import it.unibs.ingsoft.domain.proposta.ProposalValidationFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {
    @Test
    void costruttore_conFailureValido_memorizzaCampoEFailure() {
        ProposalValidationFailure.RequiredFieldMissing failure =
                new ProposalValidationFailure.RequiredFieldMissing("Titolo");

        ValidationError error = new ValidationError("Titolo", failure);

        assertAll(
                () -> assertEquals("Titolo", error.fieldName()),
                () -> assertSame(failure, error.failure())
        );
    }

    @Test
    void costruttore_conFailureNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ValidationError("Titolo", null));
    }

    @Test
    void costruttore_conCampoNull_loAccetta() {
        ProposalValidationFailure.RequiredFieldMissing failure =
                new ProposalValidationFailure.RequiredFieldMissing("Titolo");

        ValidationError error = new ValidationError(null, failure);

        assertAll(
                () -> assertNull(error.fieldName()),
                () -> assertSame(failure, error.failure())
        );
    }

    @Test
    void equalityHashCodeEToString_siComportanoComeValueObject() {
        ProposalValidationFailure.RequiredFieldMissing failure =
                new ProposalValidationFailure.RequiredFieldMissing("Titolo");
        ValidationError first = new ValidationError("Titolo", failure);
        ValidationError second = new ValidationError("Titolo", failure);

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertTrue(first.toString().contains("Titolo"))
        );
    }

    @Test
    void validationFailure_conTermineNonFuturo_conservaDataOdierna() {
        LocalDate oggi = LocalDate.of(2026, 5, 13);
        ValidationError error = new ValidationError(
                "Termine",
                new ProposalValidationFailure.SubscriptionDeadlineNotFuture(oggi));

        ProposalValidationFailure.SubscriptionDeadlineNotFuture failure =
                assertInstanceOf(ProposalValidationFailure.SubscriptionDeadlineNotFuture.class, error.failure());

        assertEquals(oggi, failure.today());
    }
}
