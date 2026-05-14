package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.validation.DefaultTypeValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTypeValidatorTest {
    @Test
    void validate_conInputNull_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate(null, TipoDato.INTERO).isEmpty());
    }

    @Test
    void validate_conInputBlank_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("   ", TipoDato.STRINGA).isEmpty());
    }

    @Test
    void validate_conStringaQualsiasi_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("abc", TipoDato.STRINGA).isEmpty());
    }

    @Test
    void validate_conInteroValido_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("42", TipoDato.INTERO).isEmpty());
    }

    @Test
    void validate_conInteroNonNumerico_restituisceMessaggioErrore() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("quarantadue", TipoDato.INTERO).isPresent());
    }

    @Test
    void validate_conDecimaleConVirgola_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("12,50", TipoDato.DECIMALE).isEmpty());
    }

    @Test
    void validate_conDecimaleConPunto_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("12.50", TipoDato.DECIMALE).isEmpty());
    }

    @Test
    void validate_conDecimaleNonNumerico_restituisceMessaggioErrore() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("dodici", TipoDato.DECIMALE).isPresent());
    }

    @Test
    void validate_conDataNelFormatoItaliano_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("25/12/2026", TipoDato.DATA).isEmpty());
    }

    @Test
    void validate_conDataIso_restituisceMessaggioErrore() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("2026-12-25", TipoDato.DATA).isPresent());
    }

    @Test
    void validate_conOraValida_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("16:30", TipoDato.ORA).isEmpty());
    }

    @Test
    void validate_conOraSenzaZeroDeiMinuti_restituisceMessaggioErrore() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("16:3", TipoDato.ORA).isPresent());
    }

    @Test
    void validate_conBooleanoAffermativoValido_restituisceNull() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("si", TipoDato.BOOLEANO).isEmpty());
    }

    @Test
    void validate_conBooleanoNonRiconosciuto_restituisceMessaggioErrore() {
        assertTrue(DefaultTypeValidator.INSTANCE.validate("forse", TipoDato.BOOLEANO).isPresent());
    }
}
