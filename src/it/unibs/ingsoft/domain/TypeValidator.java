package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.error.ValidationError;

/**
 * Valida una stringa grezza rispetto al {@link TipoDato} atteso.
 *
 * @return un messaggio di errore, o {@code null} se il valore è valido per quel tipo
 */
@FunctionalInterface
public interface TypeValidator {
    ValidationError validate(String input, TipoDato tipo);
}
