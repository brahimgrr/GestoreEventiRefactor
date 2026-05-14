package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.CampoBaseDefinito;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampoBaseDefinitoTest {
    @Test
    void fromNome_conNomeEsistenteInMaiuscoleDiverse_restituisceDefinizioneCorrispondente() {
        CampoBaseDefinito definizione = CampoBaseDefinito.fromNome("  titolo  ");

        assertEquals(CampoBaseDefinito.TITOLO, definizione);
    }

    @Test
    void fromNome_conNomeNull_restituisceNull() {
        assertNull(CampoBaseDefinito.fromNome(null));
    }

    @Test
    void fromNome_conNomeInesistente_restituisceNull() {
        assertNull(CampoBaseDefinito.fromNome("Campo inesistente"));
    }

    @Test
    void isNomeFisso_conNomeFisso_restituisceTrue() {
        assertTrue(CampoBaseDefinito.isNomeFisso("Data"));
    }

    @Test
    void isNomeFisso_conNomeNonFisso_restituisceFalse() {
        assertFalse(CampoBaseDefinito.isNomeFisso("Altro"));
    }

    @Test
    void isNomeFisso_conNomeNull_restituisceFalse() {
        assertFalse(CampoBaseDefinito.isNomeFisso(null));
    }

    @Test
    void toCampo_conDefinizioneTitolo_creaCampoBaseObbligatorioConTipoDatoDellaDefinizione() {
        Campo campo = CampoBaseDefinito.TITOLO.toCampo();

        assertAll(
                () -> assertEquals("Titolo", campo.getNome()),
                () -> assertEquals(TipoCampo.BASE, campo.getTipo()),
                () -> assertEquals(TipoDato.STRINGA, campo.getTipoDato()),
                () -> assertTrue(campo.isObbligatorio())
        );
    }

    @Test
    void toCampo_perOgniDefinizione_mantieneNomeETipoDato() {
        for (CampoBaseDefinito definizione : CampoBaseDefinito.values()) {
            Campo campo = definizione.toCampo();

            assertAll(
                    () -> assertEquals(definizione.getNomeCampo(), campo.getNome()),
                    () -> assertEquals(definizione.getTipoDato(), campo.getTipoDato()),
                    () -> assertEquals(TipoCampo.BASE, campo.getTipo())
            );
        }
    }
}
