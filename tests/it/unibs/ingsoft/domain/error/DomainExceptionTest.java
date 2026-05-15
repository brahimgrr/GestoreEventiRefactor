package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {
    @Test
    void costruttore_conFailure_impostaMessaggioEFutureFailure() {
        ProposalFailure.NotSavable failure = new ProposalFailure.NotSavable();

        DomainException exception = new DomainException(failure);

        assertAll(
                () -> assertSame(failure, exception.failure()),
                () -> assertEquals(failure.getClass().getName(), exception.getMessage())
        );
    }

    @Test
    void costruttore_conCause_preservaCauseEFailure() {
        RuntimeException cause = new RuntimeException("boom");
        ProposalFailure.InvalidStateTransition failure =
                new ProposalFailure.InvalidStateTransition(StatoProposta.BOZZA, StatoProposta.APERTA);

        DomainException exception = new DomainException(failure, cause);

        assertAll(
                () -> assertSame(failure, exception.failure()),
                () -> assertSame(cause, exception.getCause())
        );
    }

    @Test
    void failureTemporali_conservanoDateNelRecord() {
        LocalDate data = LocalDate.of(2026, 5, 13);

        assertAll(
                () -> assertEquals(data, new ProposalFailure.PublicationDeadlineExpired(data).deadline()),
                () -> assertEquals(data, new ProposalFailure.SubscriptionDeadlineExpired(data).deadline()),
                () -> assertEquals(data, new ProposalFailure.UnsubscriptionDeadlineExpired(data).deadline())
        );
    }
}
