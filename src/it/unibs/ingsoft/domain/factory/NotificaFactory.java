package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Notifica;

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
}
