package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.NotificaType;
import it.unibs.ingsoft.domain.Proposta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralizza la creazione delle notifiche applicative.
 */
public final class NotificaFactory {
    private static NotificaFactory instance;

    private NotificaFactory() {
    }

    public static NotificaFactory getInstance() {
        if (instance == null) {
            instance = new NotificaFactory();
        }
        return instance;
    }

    public Notifica creaNotifica(String messaggio) {
        return new Notifica(messaggio);
    }

    public Notifica creaNotificaPropostaConfermata(Proposta proposta) {
        return Notifica.notificaStrutturata(NotificaType.PROPOSTA_CONFERMATA, payloadProposta(proposta));
    }

    public Notifica creaNotificaPropostaAnnullata(Proposta proposta) {
        return Notifica.notificaStrutturata(NotificaType.PROPOSTA_ANNULLATA, payloadProposta(proposta));
    }

    public Notifica creaNotificaPropostaRitirata(Proposta proposta) {
        return Notifica.notificaStrutturata(NotificaType.PROPOSTA_RITIRATA, payloadProposta(proposta));
    }

    private Map<String, String> payloadProposta(Proposta proposta) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("titolo", proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo"));
        payload.put("data", proposta.valoreCampoOrDefault(AppConstants.CAMPO_DATA, ""));
        payload.put("ora", proposta.valoreCampoOrDefault(AppConstants.CAMPO_ORA, ""));
        payload.put("luogo", proposta.valoreCampoOrDefault(AppConstants.CAMPO_LUOGO, ""));
        payload.put("quota", proposta.valoreCampoOrDefault(AppConstants.CAMPO_QUOTA, ""));
        return payload;
    }
}
