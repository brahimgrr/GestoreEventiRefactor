package it.unibs.ingsoft.domain.shared.validation;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Interfaccia non ha senso di essere testata
 */
class TypeValidator_Test {
    @Test
    void validate_conImplementazioneLambda_puoRestituireOptionalVuoto() {
        TypeValidator validator = (input, tipo) -> Optional.empty();

        assertTrue(validator.validate("abc", TipoDato.STRINGA).isEmpty());
    }
}
