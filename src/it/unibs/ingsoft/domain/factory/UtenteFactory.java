package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Configuratore;
import it.unibs.ingsoft.domain.Fruitore;

/**
 * Centralizza la creazione degli utenti autenticati o registrati.
 */
public final class UtenteFactory {
    private static UtenteFactory instance;

    private UtenteFactory() {
    }

    public static UtenteFactory getInstance() {
        if (instance == null) {
            instance = new UtenteFactory();
        }
        return instance;
    }

    public Configuratore creaConfiguratore(String username) {
        return new Configuratore(username);
    }

    public Fruitore creaFruitore(String username) {
        return new Fruitore(username);
    }
}
