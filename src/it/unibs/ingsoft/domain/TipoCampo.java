package it.unibs.ingsoft.domain;

/**
 * Classifica l'ambito di applicazione di un {@link Campo}.
 * <ul>
 *   <li>{@code BASE} - campo fisso definito dalla specifica, obbligatorio e immutabile.</li>
 *   <li>{@code COMUNE} - campo aggiuntivo condiviso da tutte le categorie.</li>
 *   <li>{@code SPECIFICO} - campo proprio di una singola categoria.</li>
 * </ul>
 */
public enum TipoCampo {
    BASE,
    COMUNE,
    SPECIFICO
}
