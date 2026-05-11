package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.ValidationError;

/**
 * Valida una stringa grezza rispetto al {@link TipoDato} atteso.
 *
 * @return un messaggio di errore, o {@code null} se il valore è valido per quel tipo
 */
@FunctionalInterface
public interface TypeValidator {
    ValidationError validate(String input, TipoDato tipo);
}
