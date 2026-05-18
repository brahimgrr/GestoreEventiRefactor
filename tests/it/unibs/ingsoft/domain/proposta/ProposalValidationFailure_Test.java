package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/*
è un interfaccia. Secondo me non ha senso testarlo ma testare direttamente le classi che ereditano
 */
class ProposalValidationFailure_Test {
    @Test
    void failureSenzaCampi_sonoDomainFailure() {
        assertAll(
                () -> assertInstanceOf(DomainFailure.class, new ProposalValidationFailure.ParticipantsNotInteger()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalValidationFailure.ParticipantsNotPositive()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalValidationFailure.EventDateTooEarly()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalValidationFailure.ClosingDateBeforeEvent())
        );
    }

    @Test
    void requiredFieldMissing_conservaFieldName() {
        ProposalValidationFailure.RequiredFieldMissing failure =
                new ProposalValidationFailure.RequiredFieldMissing("Titolo");

        assertEquals("Titolo", failure.fieldName());
    }

    @Test
    void subscriptionDeadlineNotFuture_conservaTodayAncheNull() {
        LocalDate today = LocalDate.of(2026, 5, 15);

        assertAll(
                () -> assertEquals(today,
                        new ProposalValidationFailure.SubscriptionDeadlineNotFuture(today).today()),
                () -> assertNull(new ProposalValidationFailure.SubscriptionDeadlineNotFuture(null).today())
        );
    }

    @Test
    void participantTooShort_conservaAltezze() {
        ProposalValidationFailure.ParticipantTooShort failure =
                new ProposalValidationFailure.ParticipantTooShort(2, 4);

        assertAll(
                () -> assertEquals(2, failure.currentHeight()),
                () -> assertEquals(4, failure.minHeight())
        );
    }

    @Test
    void recordEqualityHashCodeEToString_siComportanoComeValueObject() {
        ProposalValidationFailure.RequiredFieldMissing first =
                new ProposalValidationFailure.RequiredFieldMissing("Titolo");
        ProposalValidationFailure.RequiredFieldMissing second =
                new ProposalValidationFailure.RequiredFieldMissing("Titolo");

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertTrue(first.toString().contains("Titolo"))
        );
    }
}
