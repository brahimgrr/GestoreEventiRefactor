package it.unibs.ingsoft.presentation.view.cli.fruitore.notifica;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.notifica.NotificaType;

import java.util.Map;

public final class PropostaNotificaCliMessages {
    private PropostaNotificaCliMessages() {
    }

    public static void registerInto(NotificaMessageRegistry registry) {
        registry.register(NotificaType.PROPOSTA_CONFERMATA, PropostaNotificaCliMessages::propostaConfermata)
                .register(NotificaType.PROPOSTA_ANNULLATA,
                        notifica -> "La proposta \"" + titolo(notifica.payload())
                                + "\" e' stata ANNULLATA per mancato raggiungimento del numero di partecipanti.")
                .register(NotificaType.PROPOSTA_RITIRATA,
                        notifica -> "La proposta \"" + titolo(notifica.payload())
                                + "\" e' stata RITIRATA dal configuratore.")
                .register(NotificaType.LEGACY_MESSAGGIO, NotificaMessageRegistry::legacyMessage);
    }

    private static String propostaConfermata(Notifica notifica) {
        Map<String, String> payload = notifica.payload();
        String quota = payload.getOrDefault("quota", "").trim();
        String info = "La proposta \"" + titolo(payload)
                + "\" e' stata CONFERMATA.\n"
                + "Data: " + payload.getOrDefault("data", "") + "\n"
                + "Ora: " + payload.getOrDefault("ora", "") + "\n"
                + "Luogo: " + payload.getOrDefault("luogo", "") + "\n"
                + (quota.isBlank() ? "" : "Quota: " + quota + "\n");

        return info.trim();
    }

    private static String titolo(Map<String, String> payload) {
        String titolo = payload.getOrDefault("titolo", "").trim();
        return titolo.isBlank() ? "Senza titolo" : titolo;
    }
}
