package it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu;

import it.unibs.ingsoft.domain.model.utente.Fruitore;

public interface IFruitoreView {
    enum MainAction {
        BACHECA,
        DISDICI_ISCRIZIONE,
        SPAZIO_PERSONALE,
        LOGOUT
    }

    MainAction scegliAzionePrincipale(Fruitore fruitore);
}
