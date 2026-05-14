package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.domain.proposta.ProposalValidationFailure;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
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
