package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.model.catalogo.TipoCampo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TipoCampoTest {
    @Test
    void values_quandoInvocato_restituisceTipiCampoNelContrattoDelDominio() {
        assertArrayEquals(new TipoCampo[]{
                TipoCampo.BASE,
                TipoCampo.COMUNE,
                TipoCampo.SPECIFICO
        }, TipoCampo.values());
    }
}
