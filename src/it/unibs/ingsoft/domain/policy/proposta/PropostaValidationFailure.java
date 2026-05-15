package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.error.DomainFailure;

import java.time.LocalDate;

public interface PropostaValidationFailure extends DomainFailure {

    record RequiredFieldMissing(String fieldName) implements PropostaValidationFailure {
    }

    record SubscriptionDeadlineNotFuture(LocalDate today) implements PropostaValidationFailure {
    }

    record EventDateTooEarly() implements PropostaValidationFailure {
    }

    record ClosingDateBeforeEvent() implements PropostaValidationFailure {
    }

    record ParticipantTooShort(int currentHeight, int minHeight) implements PropostaValidationFailure {
    }
}
