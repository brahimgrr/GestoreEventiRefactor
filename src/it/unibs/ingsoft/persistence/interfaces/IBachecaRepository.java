package it.unibs.ingsoft.persistence.interfaces;

import it.unibs.ingsoft.domain.proposta.Bacheca;

/**
 * Repository per la bacheca delle proposte.
 */
public interface IBachecaRepository {
    /**
     * @return la bacheca corrente; mai {@code null}
     */
    Bacheca load();

    /**
     * Persiste lo stato corrente della bacheca.
     */
    void save(Bacheca bacheca);
}
