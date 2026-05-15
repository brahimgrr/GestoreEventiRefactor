package it.unibs.ingsoft.domain.model.proposta;

import it.unibs.ingsoft.domain.error.DomainFailure;

import java.time.LocalDate;

public interface ProposalFailure extends DomainFailure {

    record NullCategory() implements ProposalFailure { }

    record NullState() implements ProposalFailure { }

    record InvalidStateTransition(StatoProposta from, StatoProposta to) implements ProposalFailure {}

    record NotSavable() implements ProposalFailure { }

    record NotValidForPublication() implements ProposalFailure { }

    record PublicationDeadlineExpired(LocalDate deadline) implements ProposalFailure { }

    record NotWithdrawable() implements ProposalFailure { }

    record WithdrawalTooLate() implements ProposalFailure { }

    record NotOpenForSubscription() implements ProposalFailure { }

    record SubscriptionDeadlineExpired(LocalDate deadline) implements ProposalFailure { }

    record AlreadySubscribed() implements ProposalFailure { }

    record Full() implements ProposalFailure { }

    record NotOpenForUnsubscription() implements ProposalFailure { }

    record UnsubscriptionDeadlineExpired(LocalDate deadline) implements ProposalFailure { }

    record NotSubscribed() implements ProposalFailure { }

    record FieldsNotModifiable(StatoProposta state) implements ProposalFailure { }

    record ParticipantsMissing() implements ProposalFailure { }

    record ParticipantsNotPositive() implements ProposalFailure { }

    record ParticipantsNotInteger(String value) implements ProposalFailure { }

    record Duplicate() implements ProposalFailure { }

    record NotFound() implements ProposalFailure { }
}
