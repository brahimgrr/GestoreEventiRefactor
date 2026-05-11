package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.CampoBaseDefinito;
import it.unibs.ingsoft.domain.TipoCampo;
import it.unibs.ingsoft.domain.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampoFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(CampoFactory.getInstance(), CampoFactory.getInstance());
    }

    @Test
    void creaCampiBase_quandoInvocato_creaUnCampoPerOgniCampoBaseDefinito() {
        List<Campo> campi = CampoFactory.getInstance().creaCampiBase();

        assertEquals(CampoBaseDefinito.values().length, campi.size());
    }

    @Test
    void creaCampiBaseExtra_conListeAllineate_creaCampiBaseObbligatori() {
        List<Campo> campi = CampoFactory.getInstance()
                .creaCampiBaseExtra(List.of("Extra"), List.of(TipoDato.STRINGA));

        assertAll(
                () -> assertEquals("Extra", campi.get(0).getNome()),
                () -> assertEquals(TipoCampo.BASE, campi.get(0).getTipo()),
                () -> assertTrue(campi.get(0).isObbligatorio())
        );
    }

    @Test
    void creaCampiBaseExtra_conListaNomiNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> CampoFactory.getInstance().creaCampiBaseExtra(null, List.of(TipoDato.STRINGA)));
    }

    @Test
    void creaCampiBaseExtra_conListeDiDimensioneDiversa_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> CampoFactory.getInstance().creaCampiBaseExtra(List.of("Uno"), List.of()));
    }

    @Test
    void creaCampoComune_conParametriValidi_creaCampoComuneConObbligatorietaRichiesta() {
        Campo campo = CampoFactory.getInstance().creaCampoComune("Eta", TipoDato.INTERO, false);

        assertAll(
                () -> assertEquals(TipoCampo.COMUNE, campo.getTipo()),
                () -> assertEquals(TipoDato.INTERO, campo.getTipoDato()),
                () -> assertFalse(campo.isObbligatorio())
        );
    }

    @Test
    void creaCampoSpecifico_conNomeNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> CampoFactory.getInstance().creaCampoSpecifico(null, TipoDato.STRINGA, false));
    }
}
