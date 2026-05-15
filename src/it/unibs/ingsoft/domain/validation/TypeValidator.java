package it.unibs.ingsoft.domain.validation;

import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.error.ValidationError;

import java.util.Optional;

/**
 * Valida una stringa grezza rispetto al {@link TipoDato} atteso.
 */
@FunctionalInterface
public interface TypeValidator {
    Optional<ValidationError> validate(String input, TipoDato tipo);
}
