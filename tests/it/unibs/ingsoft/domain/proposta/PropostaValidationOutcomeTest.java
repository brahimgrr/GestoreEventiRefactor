package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.shared.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropostaValidationOutcomeTest {
    @Test
    void costruttore_conErroriNull_creaListaVuotaEValida() {
        PropostaValidationOutcome outcome = new PropostaValidationOutcome(null, null, null);

        assertAll(
                () -> assertTrue(outcome.valida()),
                () -> assertTrue(outcome.errori().isEmpty()),
                () -> assertNull(outcome.termineIscrizione()),
                () -> assertNull(outcome.dataEvento())
        );
    }

    @Test
    void costruttore_conListaVuotaNonNull_creaOutcomeValido() {
        PropostaValidationOutcome outcome = new PropostaValidationOutcome(List.of(), null, null);

        assertAll(
                () -> assertTrue(outcome.valida()),
                () -> assertTrue(outcome.errori().isEmpty())
        );
    }

    @Test
    void costruttore_copiaListaErroriERendeOutcomeImmutabile() {
        ValidationError error = new ValidationError(
                "Titolo",
                new ProposalValidationFailure.RequiredFieldMissing("Titolo"));
        List<ValidationError> errori = new ArrayList<>(List.of(error));
        LocalDate termine = LocalDate.of(2026, 5, 16);
        LocalDate data = LocalDate.of(2026, 5, 20);

        PropostaValidationOutcome outcome = new PropostaValidationOutcome(errori, termine, data);
        errori.clear();

        assertAll(
                () -> assertFalse(outcome.valida()),
                () -> assertEquals(List.of(error), outcome.errori()),
                () -> assertEquals(termine, outcome.termineIscrizione()),
                () -> assertEquals(data, outcome.dataEvento()),
                () -> assertThrows(UnsupportedOperationException.class, () -> outcome.errori().add(error))
        );
    }

    @Test
    void equalityHashCodeEToString_siComportanoComeValueObject() {
        ValidationError error = new ValidationError(
                "Titolo",
                new ProposalValidationFailure.RequiredFieldMissing("Titolo"));
        LocalDate termine = LocalDate.of(2026, 5, 16);
        LocalDate data = LocalDate.of(2026, 5, 20);
        PropostaValidationOutcome first = new PropostaValidationOutcome(List.of(error), termine, data);
        PropostaValidationOutcome second = new PropostaValidationOutcome(List.of(error), termine, data);

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertTrue(first.toString().contains("Titolo"))
        );
    }
}
