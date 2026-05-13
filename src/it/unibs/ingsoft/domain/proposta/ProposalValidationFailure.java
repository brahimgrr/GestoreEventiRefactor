package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;

import java.time.LocalDate;

public interface ProposalValidationFailure extends DomainFailure {

    record RequiredFieldMissing(String fieldName) implements ProposalValidationFailure {
    }

    record ParticipantsNotInteger() implements ProposalValidationFailure {
    }

    record ParticipantsNotPositive() implements ProposalValidationFailure {
    }

    record SubscriptionDeadlineNotFuture(LocalDate today) implements ProposalValidationFailure {
    }

    record EventDateTooEarly() implements ProposalValidationFailure {
    }

    record ClosingDateBeforeEvent() implements ProposalValidationFailure {
    }

    record ParticipantTooShort(int currentHeight, int minHeight) implements ProposalValidationFailure {
    }
}
