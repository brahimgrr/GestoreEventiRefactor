package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatoPropostaTest {
    @Test
    void canTransitionTo_daBozzaAValida_restituisceTrue() {
        assertTrue(StatoProposta.BOZZA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void canTransitionTo_daBozzaAAperta_restituisceFalse() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void canTransitionTo_daBozzaARitirata_restituisceFalse() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.RITIRATA));
    }

    @Test
    void canTransitionTo_daBozzaAAnnullata_restituisceFalse() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test
    void canTransitionTo_daBozzaAConfermata_restituisceFalse() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void canTransitionTo_daBozzaAConclusa_restituisceFalse() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    @Test
    void canTransitionTo_daValidaABozza_restituisceTrue() {
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void canTransitionTo_daValidaAAperta_restituisceTrue() {
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void canTransitionTo_daValidaAAnnullata_restituisceFalse() {
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test
    void canTransitionTo_daValidaARitirata_restituisceFalse() {
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.RITIRATA));
    }

    @Test
    void canTransitionTo_daValidaAConfermata_restituisceFalse() {
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void canTransitionTo_daValidaAConclusa_restituisceFalse() {
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    @Test
    void canTransitionTo_daApertaAdAnnullata_restituisceTrue() {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test
    void canTransitionTo_daApertaAdConfermata_restituisceTrue() {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void canTransitionTo_daApertaAdRitirata_restituisceTrue() {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.RITIRATA));
    }

    @Test
    void canTransitionTo_daApertaAdBozza_restituisceFalse() {
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void canTransitionTo_daApertaAdValida_restituisceFalse() {
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void canTransitionTo_daApertaAdConclusa_restituisceFalse() {
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    @Test
    void canTransitionTo_daConfermataAConclusa_restituisceTrue() {
        assertTrue(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    @Test
    void canTransitionTo_daConfermataARitirata_restituisceTrue() {
        assertTrue(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.RITIRATA));
    }

    @Test
    void canTransitionTo_daConfermataABozza_restituisceFalse() {
        assertFalse(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void canTransitionTo_daConfermataAValida_restituisceFalse() {
        assertFalse(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void canTransitionTo_daConfermataAdAperta_restituisceFalse() {
        assertFalse(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void canTransitionTo_daConfermataAdAnnullata_restituisceFalse() {
        assertFalse(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test
    void canTransitionTo_daAnnullataABozza_restituisceFalse() {
        assertFalse(StatoProposta.ANNULLATA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void canTransitionTo_daAnnullataAValida_restituisceFalse() {
        assertFalse(StatoProposta.ANNULLATA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void canTransitionTo_daAnnullataAdAperta_restituisceFalse() {
        assertFalse(StatoProposta.ANNULLATA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void canTransitionTo_daAnnullataAConfermata_restituisceFalse() {
        assertFalse(StatoProposta.ANNULLATA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void canTransitionTo_daAnnullataAConclusa_restituisceFalse() {
        assertFalse(StatoProposta.ANNULLATA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    @Test
    void canTransitionTo_daAnnullataARitirata_restituisceFalse() {
        assertFalse(StatoProposta.ANNULLATA.canTransitionTo(StatoProposta.RITIRATA));
    }

    @Test
    void canTransitionTo_daRitirataABozza_restituisceFalse() {
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void canTransitionTo_daRitirataAValida_restituisceFalse() {
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void canTransitionTo_daRitirataAdAperta_restituisceFalse() {
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void canTransitionTo_daRitirataAConfermata_restituisceFalse() {
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void canTransitionTo_daRitirataAConclusa_restituisceFalse() {
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    @Test
    void canTransitionTo_daRitirataAdAnnullata_restituisceFalse() {
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test
    void canTransitionTo_daConclusaABozza_restituisceFalse() {
        assertFalse(StatoProposta.CONCLUSA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void canTransitionTo_daConclusaAValida_restituisceFalse() {
        assertFalse(StatoProposta.CONCLUSA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void canTransitionTo_daConclusaAdAperta_restituisceFalse() {
        assertFalse(StatoProposta.CONCLUSA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void canTransitionTo_daConclusaAConfermata_restituisceFalse() {
        assertFalse(StatoProposta.CONCLUSA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void canTransitionTo_daConclusaARitirata_restituisceFalse() {
        assertFalse(StatoProposta.CONCLUSA.canTransitionTo(StatoProposta.RITIRATA));
    }

    @Test
    void canTransitionTo_daConclusaAdAnnullata_restituisceFalse() {
        assertFalse(StatoProposta.CONCLUSA.canTransitionTo(StatoProposta.ANNULLATA));
    }
}
