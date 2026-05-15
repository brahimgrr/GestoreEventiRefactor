package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {
    @Test
    void costruttore_conFailureValido_memorizzaCampoEFailure() {
        PropostaValidationFailure.RequiredFieldMissing failure =
                new PropostaValidationFailure.RequiredFieldMissing("Titolo");

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
                new PropostaValidationFailure.SubscriptionDeadlineNotFuture(oggi));

        PropostaValidationFailure.SubscriptionDeadlineNotFuture failure =
                assertInstanceOf(PropostaValidationFailure.SubscriptionDeadlineNotFuture.class, error.failure());

        assertEquals(oggi, failure.today());
    }
}
