package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.ValidationError;

import java.util.Optional;

/**
 * Valida una stringa grezza rispetto al {@link TipoDato} atteso.
 */
@FunctionalInterface
public interface TypeValidator {
    Optional<ValidationError> validate(String input, TipoDato tipo);
}
