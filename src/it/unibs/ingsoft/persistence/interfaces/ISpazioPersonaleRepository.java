package it.unibs.ingsoft.persistence.interfaces;

import it.unibs.ingsoft.persistence.dto.ArchivioNotificheDTO;

/**
 * Astrazione del repository per gli spazi personali (notifiche) dei fruitori.
 */
public interface ISpazioPersonaleRepository {
    /**
     * Carica l'archivio degli spazi personali, o restituisce un archivio vuoto se non esiste ancora.
     *
     * @post result != null
     */
    ArchivioNotificheDTO load();

    /**
     * Persiste tutti gli spazi personali.
     */
    void save(ArchivioNotificheDTO archivio);
}
