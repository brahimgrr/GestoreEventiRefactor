package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
/*
è un interfaccia. Secondo me non ha senso testarlo ma testare direttamente le classi che ereditano
 */
class ProposalFailure_Test {
    @Test
    void failureSenzaCampi_sonoDomainFailure() {
        assertAll(
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NullCategory()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NullState()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotSavable()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotValidForPublication()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotWithdrawable()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.WithdrawalTooLate()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotOpenForSubscription()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.AlreadySubscribed()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.Full()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotOpenForUnsubscription()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotSubscribed()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.ParticipantsMissing()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.ParticipantsNotPositive()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.Duplicate()),
                () -> assertInstanceOf(DomainFailure.class, new ProposalFailure.NotFound())
        );
    }

    @Test
    void invalidStateTransition_conservaStatiOrigineEDestinazione() {
        ProposalFailure.InvalidStateTransition failure =
                new ProposalFailure.InvalidStateTransition(StatoProposta.BOZZA, StatoProposta.APERTA);

        assertAll(
                () -> assertEquals(StatoProposta.BOZZA, failure.from()),
                () -> assertEquals(StatoProposta.APERTA, failure.to())
        );
    }

    @Test
    void failureConDeadline_conservanoData() {
        LocalDate deadline = LocalDate.of(2026, 5, 15);

        assertAll(
                () -> assertEquals(deadline, new ProposalFailure.PublicationDeadlineExpired(deadline).deadline()),
                () -> assertEquals(deadline, new ProposalFailure.SubscriptionDeadlineExpired(deadline).deadline()),
                () -> assertEquals(deadline, new ProposalFailure.UnsubscriptionDeadlineExpired(deadline).deadline())
        );
    }

    @Test
    void fieldsNotModifiable_conservaStato() {
        ProposalFailure.FieldsNotModifiable failure =
                new ProposalFailure.FieldsNotModifiable(StatoProposta.APERTA);

        assertEquals(StatoProposta.APERTA, failure.state());
    }

    @Test
    void participantsNotInteger_conservaValore() {
        ProposalFailure.ParticipantsNotInteger failure =
                new ProposalFailure.ParticipantsNotInteger("due");

        assertEquals("due", failure.value());
    }

    @Test
    void recordEqualityHashCodeEToString_siComportanoComeValueObject() {
        ProposalFailure.ParticipantsNotInteger first =
                new ProposalFailure.ParticipantsNotInteger("due");
        ProposalFailure.ParticipantsNotInteger second =
                new ProposalFailure.ParticipantsNotInteger("due");

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertTrue(first.toString().contains("due"))
        );
    }
}
