package it.unibs.ingsoft.presentation.view.cli.configuratore.menu;

import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu.IConfiguratoreView;

public final class ConfiguratoreCliView implements IConfiguratoreView {
    private static final String[] MENU_PRINCIPALE = {
            "Gestire campi COMUNI",
            "Gestire CATEGORIE e campi SPECIFICI",
            "Visualizzare categorie e campi",
            "Creare una proposta di iniziativa",
            "Pubblicare una proposta di iniziativa",
            "Visualizzare la bacheca",
            "Ritirare una proposta",
            "Visualizzare archivio proposte",
            "Importa dati da file"
    };

    private final IAppView ui;

    public ConfiguratoreCliView(IAppView ui) {
        this.ui = ui;
    }

    @Override
    public MainAction scegliAzionePrincipale() {
        ui.stampaMenu("MENU PRINCIPALE CONFIGURATORE", MENU_PRINCIPALE, "Logout");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
        ui.newLine();
        return choice == 0 ? MainAction.LOGOUT : MainAction.values()[choice - 1];
    }
}
