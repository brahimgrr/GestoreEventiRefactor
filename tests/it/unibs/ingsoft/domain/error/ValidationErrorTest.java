package it.unibs.ingsoft.domain.error;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {
    @Test
    void costruttore_conDetailsNull_normalizzaAListaVuota() {
        ValidationError error = new ValidationError(DomainErrorCode.CAMPO_OBBLIGATORIO_MANCANTE, "Titolo", null);

        assertAll(
                () -> assertTrue(error.details().isEmpty()),
                () -> assertNull(error.detail(0))
        );
    }

    @Test
    void error_conDettagli_converteDettagliInStringheEListaImmutabile() {
        ValidationError error = ValidationError.error("Data", DomainErrorCode.DATA_EVENTO_TROPPO_PRESTO, "x", 12);

        assertAll(
                () -> assertEquals(DomainErrorCode.DATA_EVENTO_TROPPO_PRESTO, error.code()),
                () -> assertEquals("Data", error.fieldName()),
                () -> assertEquals("x", error.detail(0)),
                () -> assertEquals("12", error.detail(1)),
                () -> assertNull(error.detail(-1)),
                () -> assertThrows(UnsupportedOperationException.class, () -> error.details().add("z"))
        );
    }

    @Test
    void termineIscrizioneNonFuturo_creaErroreConDataOdiernaNelDettaglio() {
        LocalDate oggi = LocalDate.of(2026, 5, 13);

        ValidationError error = ValidationError.termineIscrizioneNonFuturo("Termine", oggi);

        assertAll(
                () -> assertEquals(DomainErrorCode.TERMINE_ISCRIZIONE_NON_FUTURO, error.code()),
                () -> assertEquals("Termine", error.fieldName()),
                () -> assertEquals("2026-05-13", error.detail(0))
        );
    }
}
