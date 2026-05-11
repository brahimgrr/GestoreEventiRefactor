package it.unibs.ingsoft.persistence.interfaces;

import it.unibs.ingsoft.domain.catalogo.Catalogo;

/**
 * Astrazione del repository per il catalogo.
 * Implementazioni alternative (JSON, in-memory per i test) sono sostituibili
 * senza modificare i service.
 */
public interface ICatalogoRepository {
    /**
     * Carica il catalogo dal file, o restituisce un catalogo vuoto se non esiste ancora.
     *
     * @return il catalogo corrente; mai {@code null}
     */
    Catalogo load();

    /**
     * Persiste lo stato corrente del catalogo.
     */
    void save(Catalogo catalogo);

}
