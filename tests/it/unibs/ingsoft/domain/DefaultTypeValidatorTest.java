package it.unibs.ingsoft.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTypeValidatorTest {
    @Test
    void validate_conInputNull_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate(null, TipoDato.INTERO));
    }

    @Test
    void validate_conInputBlank_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("   ", TipoDato.STRINGA));
    }

    @Test
    void validate_conStringaQualsiasi_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("abc", TipoDato.STRINGA));
    }

    @Test
    void validate_conInteroValido_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("42", TipoDato.INTERO));
    }

    @Test
    void validate_conInteroNonNumerico_restituisceMessaggioErrore() {
        assertNotNull(DefaultTypeValidator.INSTANCE.validate("quarantadue", TipoDato.INTERO));
    }

    @Test
    void validate_conDecimaleConVirgola_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("12,50", TipoDato.DECIMALE));
    }

    @Test
    void validate_conDecimaleConPunto_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("12.50", TipoDato.DECIMALE));
    }

    @Test
    void validate_conDecimaleNonNumerico_restituisceMessaggioErrore() {
        assertNotNull(DefaultTypeValidator.INSTANCE.validate("dodici", TipoDato.DECIMALE));
    }

    @Test
    void validate_conDataNelFormatoItaliano_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("25/12/2026", TipoDato.DATA));
    }

    @Test
    void validate_conDataIso_restituisceMessaggioErrore() {
        assertNotNull(DefaultTypeValidator.INSTANCE.validate("2026-12-25", TipoDato.DATA));
    }

    @Test
    void validate_conOraValida_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("16:30", TipoDato.ORA));
    }

    @Test
    void validate_conOraSenzaZeroDeiMinuti_restituisceMessaggioErrore() {
        assertNotNull(DefaultTypeValidator.INSTANCE.validate("16:3", TipoDato.ORA));
    }

    @Test
    void validate_conBooleanoAffermativoValido_restituisceNull() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("si", TipoDato.BOOLEANO));
    }

    @Test
    void validate_conBooleanoNonRiconosciuto_restituisceMessaggioErrore() {
        assertNotNull(DefaultTypeValidator.INSTANCE.validate("forse", TipoDato.BOOLEANO));
    }
}
