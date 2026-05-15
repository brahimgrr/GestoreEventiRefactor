package it.unibs.ingsoft.application.proposta.dto;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropostaValidationResultTest {
    @Test
    void costruttore_copiaListeERendeImmutabileIlRisultato() {
        ValidationError error = new ValidationError(
                "Titolo",
                new PropostaValidationFailure.RequiredFieldMissing("Titolo"));
        Campo campo = new Campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        List<ValidationError> errori = new ArrayList<>(List.of(error));
        List<Campo> campi = new ArrayList<>(List.of(campo));

        PropostaValidationResult result = new PropostaValidationResult(false, errori, campi);
        errori.clear();
        campi.clear();

        assertAll(
                () -> assertFalse(result.valida()),
                () -> assertEquals(List.of(error), result.errori()),
                () -> assertEquals(List.of(campo), result.campiConErrore()),
                () -> assertThrows(UnsupportedOperationException.class, () -> result.errori().add(error)),
                () -> assertThrows(UnsupportedOperationException.class, () -> result.campiConErrore().add(campo))
        );
    }

    @Test
    void costruttore_conListeNull_lanciaNullPointerException() {
        Campo campo = new Campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        ValidationError error = new ValidationError(
                "Titolo",
                new PropostaValidationFailure.RequiredFieldMissing("Titolo"));

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaValidationResult(true, null, List.of(campo))),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaValidationResult(true, List.of(error), null))
        );
    }
}
