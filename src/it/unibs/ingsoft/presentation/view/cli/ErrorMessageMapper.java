package it.unibs.ingsoft.presentation.view.cli;

import it.unibs.ingsoft.application.error.ApplicationException;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.DomainErrorParameter;
import it.unibs.ingsoft.domain.error.DomainException;

public final class ErrorMessageMapper {
    private ErrorMessageMapper() {
    }

    public static String message(Exception e) {
        if (e instanceof DomainException domainException) {
            return domainMessage(domainException);
        }
        if (e instanceof ApplicationException applicationException) {
            return applicationMessage(applicationException);
        }
        return e.getMessage() == null ? e.toString() : e.getMessage();
    }

    private static String domainMessage(DomainException e) {
        return switch (e.code()) {
            case NULL_PROPOSTA_CATEGORY -> "La categoria non puo' essere null.";
            case NULL_STATO_PROPOSTA -> "Stato non puo' essere null.";
            case INVALID_STATE_TRANSITION ->
                    "Transizione non valida: "
                            + e.parameter(DomainErrorParameter.FROM, "?")
                            + " -> "
                            + e.parameter(DomainErrorParameter.TO, "?")
                            + ".";
            case PROPOSTA_NOT_SALVABILE -> "Solo una proposta VALIDA puo' essere salvata.";
            case PROPOSTA_NOT_VALID_FOR_PUBLICATION ->
                    "La proposta deve essere in stato VALIDA per essere pubblicata.";
            case PROPOSTA_PUBLICATION_DEADLINE_EXPIRED ->
                    "Non e' piu' possibile pubblicare: il termine di iscrizione ("
                            + e.parameter(DomainErrorParameter.TERMINE, "")
                            + ") E' gia' scaduto. Rivalidare la proposta.";
            case PROPOSTA_NOT_WITHDRAWABLE ->
                    "Impossibile ritirare: la proposta non e' APERTA ne' CONFERMATA.";
            case PROPOSTA_WITHDRAWAL_TOO_LATE ->
                    "Impossibile ritirare: il ritiro e' consentito solo entro il giorno precedente la data dell'evento.";
            case PROPOSTA_NOT_OPEN_FOR_SUBSCRIPTION ->
                    "Impossibile iscriversi: la proposta non e' APERTA.";
            case PROPOSTA_SUBSCRIPTION_DEADLINE_EXPIRED ->
                    "Impossibile iscriversi: il termine di iscrizione e' scaduto ("
                            + e.parameter(DomainErrorParameter.TERMINE, "")
                            + ").";
            case PROPOSTA_ALREADY_SUBSCRIBED -> "Sei gia' iscritto a questa proposta.";
            case PROPOSTA_FULL ->
                    "Impossibile iscriversi: la proposta ha già raggiunto il numero massimo di partecipanti.";
            case PROPOSTA_NOT_OPEN_FOR_UNSUBSCRIPTION ->
                    "Impossibile disdire: la proposta non e' APERTA.";
            case PROPOSTA_UNSUBSCRIPTION_DEADLINE_EXPIRED ->
                    "Impossibile disdire: il termine di iscrizione e' scaduto ("
                            + e.parameter(DomainErrorParameter.TERMINE, "")
                            + ").";
            case PROPOSTA_NOT_SUBSCRIBED -> "Non sei iscritto a questa proposta.";
            case PROPOSTA_FIELDS_NOT_MODIFIABLE ->
                    "Impossibile modificare i campi di una proposta in stato "
                            + e.parameter(DomainErrorParameter.STATO, "")
                            + ".";
            case PROPOSTA_PARTICIPANTS_MISSING ->
                    "Campo '" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non definito nella proposta.";
            case PROPOSTA_PARTICIPANTS_NOT_POSITIVE ->
                    "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' deve essere un intero positivo.";
            case PROPOSTA_PARTICIPANTS_NOT_INTEGER ->
                    "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non e' un intero valido: "
                            + e.parameter(DomainErrorParameter.VALUE, "");
        };
    }

    private static String applicationMessage(ApplicationException e) {
        return switch (e.code()) {
            case PROPOSTA_DUPLICATA -> "Esiste gia' una proposta con lo stesso Titolo, Data, Ora e Luogo.";
        };
    }
}
