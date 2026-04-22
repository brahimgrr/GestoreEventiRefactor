package it.unibs.ingsoft.persistence.api;

import it.unibs.ingsoft.domain.SpazioPersonale;

/**
 * Astrazione del repository per gli spazi personali (notifiche) dei fruitori.
 */
public interface ISpazioPersonaleRepository {
    /**
     * Restituisce lo spazio personale del fruitore; lo crea se non esiste ancora.
     *
     * @pre username != null
     * @post result != null
     */
    SpazioPersonale get(String username);

    /**
     * Persiste tutti gli spazi personali.
     */
    void save();
}
