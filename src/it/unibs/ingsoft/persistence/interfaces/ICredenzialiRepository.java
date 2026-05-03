package it.unibs.ingsoft.persistence.interfaces;

import it.unibs.ingsoft.domain.Credenziali;

/**
 * Astrazione del repository per le credenziali degli utenti.
 */
public interface ICredenzialiRepository {
    /**
     * Carica le credenziali dal file, o restituisce un oggetto vuoto se non esiste ancora.
     *
     * @return le credenziali correnti; mai {@code null}
     */
    Credenziali get();

    /**
     * Persiste lo stato corrente delle credenziali.
     */
    void save();
}
