package it.unibs.ingsoft.application.proposta.dto;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.TipoCampo;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropostaValidationResultTest {
    @Test
    void costruttore_copiaListeERendeImmutabileIlRisultato() {
        ValidationError error = ValidationError.error("Titolo", DomainErrorCode.CAMPO_OBBLIGATORIO_MANCANTE);
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
        ValidationError error = ValidationError.error("Titolo", DomainErrorCode.CAMPO_OBBLIGATORIO_MANCANTE);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaValidationResult(true, null, List.of(campo))),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaValidationResult(true, List.of(error), null))
        );
    }
}
