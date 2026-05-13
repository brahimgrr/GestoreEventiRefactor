package it.unibs.ingsoft.presentation.view.cli.fruitore.menu;

import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;

public final class FruitoreCliView implements IFruitoreView {
    private static final String[] MENU_PRINCIPALE = {
            "Visualizza bacheca (per categoria)",
            "Disdici iscrizione a una proposta",
            "Spazio Personale (Notifiche)"
    };

    private final IAppView ui;

    public FruitoreCliView(IAppView ui) {
        this.ui = ui;
    }

    @Override
    public MainAction scegliAzionePrincipale(Fruitore fruitore) {
        ui.stampaMenu("MENU PRINCIPALE FRUITORE", MENU_PRINCIPALE, "Logout");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
        ui.newLine();
        return choice == 0 ? MainAction.LOGOUT : MainAction.values()[choice - 1];
    }
}
