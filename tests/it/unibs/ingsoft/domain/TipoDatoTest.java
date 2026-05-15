package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TipoDatoTest {
    @Test
    void values_quandoInvocato_restituisceTipiDatoNelContrattoDelDominio() {
        assertArrayEquals(new TipoDato[]{
                TipoDato.STRINGA,
                TipoDato.INTERO,
                TipoDato.DECIMALE,
                TipoDato.DATA,
                TipoDato.ORA,
                TipoDato.BOOLEANO
        }, TipoDato.values());
    }
}
