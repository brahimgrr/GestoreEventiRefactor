package it.unibs.ingsoft.domain.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
è un enum. Secondo me non ha senso testarlo ma codex ha generato quindi lascio temporaneamente
 */
class CampoBaseDefinito_Test {
    @Test
    void fromNome_conNomeEsistenteInMaiuscoleDiverse_restituisceDefinizioneCorrispondente() {
        CampoBaseDefinito definizione = CampoBaseDefinito.fromNome("  titolo  ");

        assertEquals(CampoBaseDefinito.TITOLO, definizione);
    }

    @Test
    void fromNome_perOgniNomeCanonico_restituisceLaDefinizioneCorrispondente() {
        for (CampoBaseDefinito definizione : CampoBaseDefinito.values()) {
            assertSame(definizione, CampoBaseDefinito.fromNome(definizione.getNomeCampo()));
        }
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

    @Test
    void valueOf_conNomeCostanteRestituisceCostante() {
        assertSame(CampoBaseDefinito.DATA, CampoBaseDefinito.valueOf("DATA"));
    }
}
