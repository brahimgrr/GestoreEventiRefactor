package it.unibs.ingsoft.persistence.interfaces;

import it.unibs.ingsoft.domain.Bacheca;

/**
 * Repository per la bacheca delle proposte.
 */
public interface IBachecaRepository {
    /**
     * @return la bacheca corrente; mai {@code null}
     */
    Bacheca get();

    /**
     * Persiste lo stato corrente della bacheca.
     */
    void save();
}
