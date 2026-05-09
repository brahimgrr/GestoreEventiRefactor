package it.unibs.ingsoft.domain;

public final class NotificaFormatter   { // notificaview

    private NotificaFormatter() {
    }


//    public static String render(Notifica notifica){
//        switch (notifica.type) {
//            case NotificaType.CONFERMATA:
//                return propostaConfermata(notifica.getProposta*())
//        }
//    }

    public static String propostaConfermata(Proposta proposta) {
        String quota = proposta.valoreCampoOrDefault(AppConstants.CAMPO_QUOTA, "").trim();
        String info = "La proposta \"" + proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo")
                + "\" e' stata CONFERMATA.\n"
                + "Data: " + proposta.valoreCampoOrDefault(AppConstants.CAMPO_DATA, "") + "\n"
                + "Ora: " + proposta.valoreCampoOrDefault(AppConstants.CAMPO_ORA, "") + "\n"
                + "Luogo: " + proposta.valoreCampoOrDefault(AppConstants.CAMPO_LUOGO, "") + "\n"
                + (quota.isBlank() ? "" : "Quota: " + quota + "\n");

        return info.trim();
    }

    public static String propostaAnnullata(Proposta proposta) {
        return "La proposta \"" + proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo")
                + "\" e' stata ANNULLATA per mancato raggiungimento del numero di partecipanti.";
    }

    public static String propostaRitirata(Proposta proposta) {
        String titolo = proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo");
        return "La proposta \"" + titolo
                + "\" e' stata RITIRATA dal configuratore.";
    }
}
