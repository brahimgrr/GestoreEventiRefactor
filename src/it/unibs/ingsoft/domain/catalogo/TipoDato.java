package it.unibs.ingsoft.domain.catalogo;

/**
 * Tipo di dato del valore di un campo.
 * Descrive il formato atteso e viene usato per validare l'input utente.
 */
public enum TipoDato {
    STRINGA,
    INTERO,
    DECIMALE,
    DATA,
    ORA,
    BOOLEANO
}
