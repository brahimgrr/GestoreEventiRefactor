package it.unibs.ingsoft.domain.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportErrorTest {
    @Test
    void costruttore_conDetailsNull_normalizzaAListaVuota() {
        ImportError error = new ImportError(DomainErrorCode.IMPORT_FILE_NON_TROVATO, null, null, null);

        assertAll(
                () -> assertTrue(error.details().isEmpty()),
                () -> assertNull(error.detail(0))
        );
    }

    @Test
    void of_conDettagli_converteDettagliEListaImmutabile() {
        ImportError error = ImportError.of(DomainErrorCode.IMPORT_CAMPO_COMUNE_DUPLICATO, "Note", 2);

        assertAll(
                () -> assertEquals("Note", error.detail(0)),
                () -> assertEquals("2", error.detail(1)),
                () -> assertNull(error.detail(-1)),
                () -> assertThrows(UnsupportedOperationException.class, () -> error.details().add("x"))
        );
    }

    @Test
    void withDomainError_collegaEccezioneDominio() {
        DomainException domainException = new DomainException(DomainErrorCode.CATALOGO_CAMPO_DUPLICATO, "Note");

        ImportError error = ImportError.withDomainError(
                DomainErrorCode.IMPORT_CAMPO_COMUNE_ERRORE_DOMINIO,
                domainException,
                "Note");

        assertAll(
                () -> assertSame(domainException, error.domainException()),
                () -> assertNull(error.validationError()),
                () -> assertEquals("Note", error.detail(0))
        );
    }

    @Test
    void withValidationError_collegaErroreValidazione() {
        ValidationError validationError = ValidationError.error("Data", DomainErrorCode.TIPO_DATA_NON_VALIDA);

        ImportError error = ImportError.withValidationError(
                DomainErrorCode.IMPORT_PROPOSTA_CAMPO_TIPO_NON_VALIDO,
                validationError,
                "Torneo");

        assertAll(
                () -> assertSame(validationError, error.validationError()),
                () -> assertNull(error.domainException()),
                () -> assertEquals("Torneo", error.detail(0))
        );
    }
}
