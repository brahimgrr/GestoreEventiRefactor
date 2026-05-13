package it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu;

public interface IConfiguratoreView {
    enum MainAction {
        CAMPI_COMUNI,
        CATEGORIE,
        VISUALIZZA,
        CREA_PROPOSTA,
        PUBBLICA_PROPOSTA,
        BACHECA,
        RITIRA_PROPOSTA,
        ARCHIVIO,
        IMPORTA,
        LOGOUT
    }

    MainAction scegliAzionePrincipale();
}
