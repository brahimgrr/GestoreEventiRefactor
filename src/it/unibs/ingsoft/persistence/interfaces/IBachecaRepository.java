package it.unibs.ingsoft.persistence.interfaces;

import it.unibs.ingsoft.persistence.dto.BachecaDTO;

/**
 * Repository per la bacheca delle proposte.
 */
public interface IBachecaRepository {
    /**
     * @return la bacheca corrente; mai {@code null}
     */
    BachecaDTO load();

    /**
     * Persiste lo stato corrente della bacheca.
     */
    void save(BachecaDTO bacheca);
}
