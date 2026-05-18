package it.unibs.ingsoft.domain.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/*
è un enum. Secondo me non ha senso testarlo ma codex ha generato quindi lascio temporaneamente
 */
class TipoCampo_Test {
    @Test
    void values_quandoInvocato_restituisceTipiCampoNelContrattoDelDominio() {
        assertArrayEquals(new TipoCampo[]{
                TipoCampo.BASE,
                TipoCampo.COMUNE,
                TipoCampo.SPECIFICO
        }, TipoCampo.values());
    }

    @Test
    void valueOf_conNomeCostanteRestituisceCostante() {
        assertSame(TipoCampo.COMUNE, TipoCampo.valueOf("COMUNE"));
    }
}
