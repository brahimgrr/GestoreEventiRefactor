package it.unibs.ingsoft.presentation.view.cli;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;

import java.util.List;

public final class DomainErrorMessageMapper {
    private DomainErrorMessageMapper() {
    }

    public static String message(Exception e) {
        if (e instanceof DomainException domainException) {
            return message(domainException);
        }
        return e.getMessage() == null ? e.toString() : e.getMessage();
    }

    public static String message(DomainException e) {
        return message(e.code(), e.details());
    }

    public static String message(DomainErrorCode code, List<String> details) {
        return switch (code) {
            case CAMPO_NOME_NON_VALIDO -> "Il nome del campo non puo essere vuoto.";
            case CAMPO_TIPO_NON_VALIDO -> "Il tipo del campo non puo essere null.";
            case CAMPO_TIPO_DATO_NON_VALIDO -> "Il tipo dato del campo non puo essere null.";
            case CAMPO_EXTRA_DATI_NON_VALIDI -> "I dati dei campi extra non possono essere null.";
            case CAMPO_EXTRA_DIMENSIONI_NON_COHERENTI ->
                    "Nomi e tipi dei campi extra devono avere la stessa dimensione.";
            case CATEGORIA_NOME_NON_VALIDO -> "Il nome della categoria non puo essere vuoto.";
            case CATEGORIA_CAMPO_NON_SPECIFICO ->
                    "Solo campi di tipo SPECIFICO possono essere aggiunti a una categoria.";
            case CATEGORIA_CAMPO_DUPLICATO ->
                    "La categoria \"" + detail(details, 0, "") + "\" ha gia un campo chiamato \""
                            + detail(details, 1, "") + "\".";
            case CATALOGO_CAMPI_BASE_GIA_FISSATI -> "Campi base gia fissati.";
            case CATALOGO_CAMPO_DUPLICATO -> {
                String campo = detail(details, 0, "");
                yield campo.isBlank() ? "Campo gia esistente." : "Campo gia esistente: " + campo;
            }
            case CATALOGO_NOME_DUPLICATO -> "Duplicato: " + detail(details, 0, "");
            case CATALOGO_CATEGORIA_DUPLICATA -> "Categoria gia esistente.";
            case CATALOGO_CATEGORIA_NON_TROVATA -> "Categoria non trovata.";
            case PERSONA_USERNAME_NON_VALIDO -> "Lo username non puo essere vuoto.";
            case AUTH_USERNAME_NON_VALIDO -> "Username non valido.";
            case AUTH_PASSWORD_NON_VALIDA -> "Password non valida.";
            case AUTH_USERNAME_TROPPO_CORTO ->
                    "Username troppo corto (minimo " + detail(details, 0, "") + " caratteri).";
            case AUTH_PASSWORD_TROPPO_CORTA ->
                    "Password troppo corta (minimo " + detail(details, 0, "") + " caratteri).";
            case AUTH_USERNAME_RISERVATO -> "Username riservato. Scegli un nome diverso.";
            case AUTH_USERNAME_GIA_IN_USO -> "Username gia in uso. Scegli un nome diverso.";
            case NULL_PROPOSTA_CATEGORY -> "La categoria non puo' essere null.";
            case NULL_STATO_PROPOSTA -> "Stato non puo' essere null.";
            case INVALID_STATE_TRANSITION ->
                    "Transizione non valida: " + detail(details, 0, "?") + " -> " + detail(details, 1, "?") + ".";
            case PROPOSTA_NOT_SALVABILE -> "Solo una proposta VALIDA puo' essere salvata.";
            case PROPOSTA_NOT_VALID_FOR_PUBLICATION ->
                    "La proposta deve essere in stato VALIDA per essere pubblicata.";
            case PROPOSTA_PUBLICATION_DEADLINE_EXPIRED ->
                    "Non e' piu' possibile pubblicare: il termine di iscrizione ("
                            + detail(details, 0, "") + ") E' gia' scaduto. Rivalidare la proposta.";
            case PROPOSTA_NOT_WITHDRAWABLE ->
                    "Impossibile ritirare: la proposta non e' APERTA ne' CONFERMATA.";
            case PROPOSTA_WITHDRAWAL_TOO_LATE ->
                    "Impossibile ritirare: il ritiro e' consentito solo entro il giorno precedente la data dell'evento.";
            case PROPOSTA_NOT_OPEN_FOR_SUBSCRIPTION ->
                    "Impossibile iscriversi: la proposta non e' APERTA.";
            case PROPOSTA_SUBSCRIPTION_DEADLINE_EXPIRED ->
                    "Impossibile iscriversi: il termine di iscrizione e' scaduto ("
                            + detail(details, 0, "") + ").";
            case PROPOSTA_ALREADY_SUBSCRIBED -> "Sei gia' iscritto a questa proposta.";
            case PROPOSTA_FULL ->
                    "Impossibile iscriversi: la proposta ha gia' raggiunto il numero massimo di partecipanti.";
            case PROPOSTA_NOT_OPEN_FOR_UNSUBSCRIPTION ->
                    "Impossibile disdire: la proposta non e' APERTA.";
            case PROPOSTA_UNSUBSCRIPTION_DEADLINE_EXPIRED ->
                    "Impossibile disdire: il termine di iscrizione e' scaduto ("
                            + detail(details, 0, "") + ").";
            case PROPOSTA_NOT_SUBSCRIBED -> "Non sei iscritto a questa proposta.";
            case PROPOSTA_FIELDS_NOT_MODIFIABLE ->
                    "Impossibile modificare i campi di una proposta in stato " + detail(details, 0, "") + ".";
            case PROPOSTA_PARTICIPANTS_MISSING ->
                    "Campo '" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non definito nella proposta.";
            case PROPOSTA_PARTICIPANTS_NOT_POSITIVE ->
                    "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' deve essere un intero positivo.";
            case PROPOSTA_PARTICIPANTS_NOT_INTEGER ->
                    "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non e' un intero valido: "
                            + detail(details, 0, "");
            case PROPOSTA_DUPLICATA -> "Esiste gia' una proposta con lo stesso Titolo, Data, Ora e Luogo.";
            case PROPOSTA_NON_TROVATA -> "Proposta non trovata o non piu' disponibile.";
            case IMPORT_FILE_NON_TROVATO -> "File non trovato: " + detail(details, 0, "");
            case IMPORT_FILE_NON_LEGGIBILE -> "File non leggibile: " + detail(details, 0, "");
            default -> code.name();
        };
    }

    static String detail(List<String> details, int index, String defaultValue) {
        return index >= 0 && index < details.size() ? details.get(index) : defaultValue;
    }
}
