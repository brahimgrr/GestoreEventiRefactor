package it.unibs.ingsoft.domain.model.catalogo;

/**
 * Tipo di dato del valore di un campo.
 * Descrive il formato atteso e viene usato per validare l'input utente.
 */
public enum TipoDato {
    STRINGA,
    INTERO,
    INTERO_POSITIVO,
    DECIMALE,
    DATA,
    ORA,
    BOOLEANO
}
