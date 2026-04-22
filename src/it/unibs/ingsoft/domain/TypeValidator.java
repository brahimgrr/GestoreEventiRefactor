package it.unibs.ingsoft.domain;

/**
 * Valida una stringa grezza rispetto al {@link TipoDato} atteso.
 *
 * @return un messaggio di errore, o {@code null} se il valore è valido per quel tipo
 */
@FunctionalInterface
public interface TypeValidator {
    String validate(String input, TipoDato tipo);
}
