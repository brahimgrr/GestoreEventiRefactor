package it.unibs.ingsoft.domain.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/*
è un enum. Secondo me non ha senso testarlo ma codex ha generato quindi lascio temporaneamente
 */
class TipoDato_Test {
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

    @Test
    void valueOf_conNomeCostanteRestituisceCostante() {
        assertSame(TipoDato.DECIMALE, TipoDato.valueOf("DECIMALE"));
    }
}
