package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;

public final class ProposalFailureCliMessages {
    private ProposalFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry
                .register(ProposalFailure.NullCategory.class, (failure, messages) ->
                        "La categoria non puo' essere null.")
                .register(ProposalFailure.NullState.class, (failure, messages) ->
                        "Stato non puo' essere null.")
                .register(ProposalFailure.InvalidStateTransition.class, (failure, messages) ->
                        "Transizione non valida: " + failure.from() + " -> " + failure.to() + ".")
                .register(ProposalFailure.NotSavable.class, (failure, messages) ->
                        "Solo una proposta VALIDA puo' essere salvata.")
                .register(ProposalFailure.NotValidForPublication.class, (failure, messages) ->
                        "La proposta deve essere in stato VALIDA per essere pubblicata.")
                .register(ProposalFailure.PublicationDeadlineExpired.class, (failure, messages) ->
                        "Non e' piu' possibile pubblicare: il termine di iscrizione ("
                                + failure.deadline() + ") E' gia' scaduto. Rivalidare la proposta.")
                .register(ProposalFailure.NotWithdrawable.class, (failure, messages) ->
                        "Impossibile ritirare: la proposta non e' APERTA ne' CONFERMATA.")
                .register(ProposalFailure.WithdrawalTooLate.class, (failure, messages) ->
                        "Impossibile ritirare: il ritiro e' consentito solo entro il giorno precedente la data dell'evento.")
                .register(ProposalFailure.NotOpenForSubscription.class, (failure, messages) ->
                        "Impossibile iscriversi: la proposta non e' APERTA.")
                .register(ProposalFailure.SubscriptionDeadlineExpired.class, (failure, messages) ->
                        "Impossibile iscriversi: il termine di iscrizione e' scaduto ("
                                + failure.deadline() + ").")
                .register(ProposalFailure.AlreadySubscribed.class, (failure, messages) ->
                        "Sei gia' iscritto a questa proposta.")
                .register(ProposalFailure.Full.class, (failure, messages) ->
                        "Impossibile iscriversi: la proposta ha gia' raggiunto il numero massimo di partecipanti.")
                .register(ProposalFailure.NotOpenForUnsubscription.class, (failure, messages) ->
                        "Impossibile disdire: la proposta non e' APERTA.")
                .register(ProposalFailure.UnsubscriptionDeadlineExpired.class, (failure, messages) ->
                        "Impossibile disdire: il termine di iscrizione e' scaduto ("
                                + failure.deadline() + ").")
                .register(ProposalFailure.NotSubscribed.class, (failure, messages) ->
                        "Non sei iscritto a questa proposta.")
                .register(ProposalFailure.FieldsNotModifiable.class, (failure, messages) ->
                        "Impossibile modificare i campi di una proposta in stato " + failure.state() + ".")
                .register(ProposalFailure.ParticipantsMissing.class, (failure, messages) ->
                        "Campo '" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non definito nella proposta.")
                .register(ProposalFailure.ParticipantsNotPositive.class, (failure, messages) ->
                        "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' deve essere un intero positivo.")
                .register(ProposalFailure.ParticipantsNotInteger.class, (failure, messages) ->
                        "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non e' un intero valido: "
                                + failure.value())
                .register(ProposalFailure.Duplicate.class, (failure, messages) ->
                        "Esiste gia' una proposta con lo stesso Titolo, Data, Ora e Luogo.")
                .register(ProposalFailure.NotFound.class, (failure, messages) ->
                        "Proposta non trovata o non piu' disponibile.");
    }
}
