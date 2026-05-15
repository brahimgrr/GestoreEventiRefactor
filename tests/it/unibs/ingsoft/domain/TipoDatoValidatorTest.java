package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidator;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategyRegistry;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TipoDatoValidatorTest {
    @Test
    void validate_conInputNull_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate(null, TipoDato.INTERO).isEmpty());
    }

    @Test
    void validate_conInputBlank_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("   ", TipoDato.STRINGA).isEmpty());
    }

    @Test
    void validate_conStringaQualsiasi_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("abc", TipoDato.STRINGA).isEmpty());
    }

    @Test
    void validate_conInteroValido_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("42", TipoDato.INTERO).isEmpty());
    }

    @Test
    void validate_conInteroPositivoValido_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("42", TipoDato.INTERO_POSITIVO).isEmpty());
    }

    @Test
    void validate_conInteroNonNumerico_restituisceMessaggioErrore() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("quarantadue", TipoDato.INTERO).isPresent());
    }

    @Test
    void validate_conInteroPositivoNonPositivo_restituisceErroreSpecifico() {
        assertInstanceOf(
                TypeValidationFailure.InvalidPositiveInteger.class,
                TipoDatoValidator.INSTANCE.validate("0", TipoDato.INTERO_POSITIVO).orElseThrow().failure());
    }

    @Test
    void validate_conInteroPositivoNonNumerico_restituisceInvalidInteger() {
        assertInstanceOf(
                TypeValidationFailure.InvalidInteger.class,
                TipoDatoValidator.INSTANCE.validate("zero", TipoDato.INTERO_POSITIVO).orElseThrow().failure());
    }

    @Test
    void validate_conDecimaleConVirgola_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("12,50", TipoDato.DECIMALE).isEmpty());
    }

    @Test
    void validate_conDecimaleConPunto_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("12.50", TipoDato.DECIMALE).isEmpty());
    }

    @Test
    void validate_conDecimaleNonNumerico_restituisceMessaggioErrore() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("dodici", TipoDato.DECIMALE).isPresent());
    }

    @Test
    void validate_conDataNelFormatoItaliano_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("25/12/2026", TipoDato.DATA).isEmpty());
    }

    @Test
    void validate_conDataIso_restituisceMessaggioErrore() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("2026-12-25", TipoDato.DATA).isPresent());
    }

    @Test
    void validate_conOraValida_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("16:30", TipoDato.ORA).isEmpty());
    }

    @Test
    void validate_conOraSenzaZeroDeiMinuti_restituisceMessaggioErrore() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("16:3", TipoDato.ORA).isPresent());
    }

    @Test
    void validate_conBooleanoAffermativoValido_restituisceNull() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("si", TipoDato.BOOLEANO).isEmpty());
    }

    @Test
    void validate_conBooleaniMaiuscoliEAccento_restituisceNull() {
        assertAll(
                () -> assertTrue(TipoDatoValidator.INSTANCE.validate(" TRUE ", TipoDato.BOOLEANO).isEmpty()),
                () -> assertTrue(TipoDatoValidator.INSTANCE.validate("SÌ", TipoDato.BOOLEANO).isEmpty()),
                () -> assertTrue(TipoDatoValidator.INSTANCE.validate("No", TipoDato.BOOLEANO).isEmpty()),
                () -> assertTrue(TipoDatoValidator.INSTANCE.validate("FALSE", TipoDato.BOOLEANO).isEmpty())
        );
    }

    @Test
    void validate_conBooleanoNonRiconosciuto_restituisceMessaggioErrore() {
        assertTrue(TipoDatoValidator.INSTANCE.validate("forse", TipoDato.BOOLEANO).isPresent());
    }

    @Test
    void validate_conValoriInvalidi_restituisceCodiceErroreSpecifico() {
        assertAll(
                () -> assertInstanceOf(TypeValidationFailure.InvalidInteger.class,
                        TipoDatoValidator.INSTANCE.validate("x", TipoDato.INTERO).orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidInteger.class,
                        TipoDatoValidator.INSTANCE.validate("x", TipoDato.INTERO_POSITIVO).orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidDecimal.class,
                        TipoDatoValidator.INSTANCE.validate("x", TipoDato.DECIMALE).orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidDate.class,
                        TipoDatoValidator.INSTANCE.validate("x", TipoDato.DATA).orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidTime.class,
                        TipoDatoValidator.INSTANCE.validate("x", TipoDato.ORA).orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidBoolean.class,
                        TipoDatoValidator.INSTANCE.validate("x", TipoDato.BOOLEANO).orElseThrow().failure())
        );
    }

    @Test
    void validate_conStrategiaCustomPerTipoDato_delegaAllaStrategiaIniettata() {
        TipoDatoValidationStrategyRegistry registry = TipoDatoValidationStrategyRegistry.of(Map.of(
                TipoDato.STRINGA,
                value -> Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidBoolean()))
        ));
        TipoDatoValidator validator = new TipoDatoValidator(registry);

        assertInstanceOf(
                TypeValidationFailure.InvalidBoolean.class,
                validator.validate("qualunque", TipoDato.STRINGA).orElseThrow().failure());
    }

    @Test
    void registryStandard_contieneStrategiaPerOgniTipoDatoDelDominio() {
        TipoDatoValidationStrategyRegistry registry = TipoDatoValidationStrategyRegistry.standard();

        assertAll(
                () -> assertTrue(registry.getStrategy(TipoDato.STRINGA).valida("abc").isEmpty()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidInteger.class,
                        registry.getStrategy(TipoDato.INTERO).valida("x").orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidPositiveInteger.class,
                        registry.getStrategy(TipoDato.INTERO_POSITIVO).valida("0").orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidDecimal.class,
                        registry.getStrategy(TipoDato.DECIMALE).valida("x").orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidDate.class,
                        registry.getStrategy(TipoDato.DATA).valida("x").orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidTime.class,
                        registry.getStrategy(TipoDato.ORA).valida("x").orElseThrow().failure()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidBoolean.class,
                        registry.getStrategy(TipoDato.BOOLEANO).valida("x").orElseThrow().failure())
        );
    }
}
