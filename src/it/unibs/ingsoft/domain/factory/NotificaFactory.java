package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Notifica;

/**
 * Centralizza la creazione delle notifiche applicative.
 */
public final class NotificaFactory {

    public Notifica creaNotifica(String messaggio) {
        return new Notifica(messaggio);
    }
}
