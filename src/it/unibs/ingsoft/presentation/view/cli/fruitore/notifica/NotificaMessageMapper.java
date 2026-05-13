package it.unibs.ingsoft.presentation.view.cli.fruitore.notifica;

import it.unibs.ingsoft.domain.notifica.Notifica;

import java.util.Map;

public final class NotificaMessageMapper {
    private NotificaMessageMapper() {
    }

    public static String message(Notifica notifica) {
        Map<String, String> payload = notifica.payload();
        return switch (notifica.type()) {
            case PROPOSTA_CONFERMATA -> propostaConfermata(payload);
            case PROPOSTA_ANNULLATA -> "La proposta \"" + titolo(payload)
                    + "\" e' stata ANNULLATA per mancato raggiungimento del numero di partecipanti.";
            case PROPOSTA_RITIRATA -> "La proposta \"" + titolo(payload)
                    + "\" e' stata RITIRATA dal configuratore.";
            case LEGACY_MESSAGGIO -> notifica.messaggio() == null ? "" : notifica.messaggio();
        };
    }

    private static String propostaConfermata(Map<String, String> payload) {
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
