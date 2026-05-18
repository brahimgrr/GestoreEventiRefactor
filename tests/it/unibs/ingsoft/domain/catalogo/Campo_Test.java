package it.unibs.ingsoft.domain.catalogo;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
C'è un instance of a riga 79 di Campo
 */
class Campo_Test {
    @Test
    void costruttore_conNomeValidoTrimmato_salvaNomeSenzaSpaziEsterni() {
        Campo campo = new Campo("  Nome  ", TipoCampo.COMUNE, TipoDato.STRINGA, true);

        assertEquals("Nome", campo.getNome());
    }

    @Test
    void costruttore_conNomeNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Campo(null, TipoCampo.COMUNE, TipoDato.STRINGA, true));

        assertInstanceOf(CatalogFailure.FieldNameInvalid.class, exception.failure());
    }

    @Test
    void costruttore_conNomeBlank_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Campo("   ", TipoCampo.COMUNE, TipoDato.STRINGA, true));

        assertInstanceOf(CatalogFailure.FieldNameInvalid.class, exception.failure());
    }

    @Test
    void costruttore_conTipoNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Campo("Nome", null, TipoDato.STRINGA, true));

        assertInstanceOf(CatalogFailure.FieldTypeInvalid.class, exception.failure());
    }

    @Test
    void costruttore_conTipoDatoNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Campo("Nome", TipoCampo.COMUNE, null, true));

        assertInstanceOf(CatalogFailure.FieldDataTypeInvalid.class, exception.failure());
    }

    @Test
    void withObbligatorio_quandoCambiaFlag_restituisceNuovoCampoConStessoNomeTipoETipoDato() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        Campo aggiornato = campo.withObbligatorio(true);

        assertAll(
                () -> assertNotSame(campo, aggiornato),
                () -> assertEquals("Nome", aggiornato.getNome()),
                () -> assertEquals(TipoCampo.COMUNE, aggiornato.getTipo()),
                () -> assertEquals(TipoDato.STRINGA, aggiornato.getTipoDato()),
                () -> assertTrue(aggiornato.isObbligatorio())
        );
    }

    @Test
    void withObbligatorio_quandoFlagUguale_restituisceNuovoCampoConStessoFlag() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, true);

        Campo aggiornato = campo.withObbligatorio(true);

        assertAll(
                () -> assertNotSame(campo, aggiornato),
                () -> assertTrue(aggiornato.isObbligatorio())
        );
    }

    @Test
    void costruttoreDiCopia_conCampoEsistente_copiaTuttiGliAttributiInNuovaIstanza() {
        Campo originale = new Campo("Nome", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, true);

        Campo copia = new Campo(originale);

        assertAll(
                () -> assertNotSame(originale, copia),
                () -> assertEquals(originale.getNome(), copia.getNome()),
                () -> assertEquals(originale.getTipo(), copia.getTipo()),
                () -> assertEquals(originale.getTipoDato(), copia.getTipoDato()),
                () -> assertEquals(originale.isObbligatorio(), copia.isObbligatorio())
        );
    }

    @Test
    void costruttoreDiCopia_conCampoNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Campo(null));
    }

    @Test
    void equals_conStessoNomeInMaiuscoleDiverse_restituisceTrue() {
        Campo primo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);
        Campo secondo = new Campo("nome", TipoCampo.SPECIFICO, TipoDato.INTERO, true);

        assertEquals(primo, secondo);
    }

    @Test
    void equals_conStessaIstanza_restituisceTrue() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        assertEquals(campo, campo);
    }

    @Test
    void equals_conOggettoDiTipoDiverso_restituisceFalse() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        assertNotEquals("Nome", campo);
    }

    @Test
    void equals_conOggettoNull_restituisceFalse() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        assertNotEquals(null, campo);
    }

    @Test
    void equals_conNomeDiverso_restituisceFalse() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        assertNotEquals(campo, new Campo("Altro", TipoCampo.COMUNE, TipoDato.STRINGA, false));
    }

    @Test
    void hashCode_conStessoNomeInMaiuscoleDiverse_restituisceStessoHashCode() {
        Campo primo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);
        Campo secondo = new Campo("nome", TipoCampo.SPECIFICO, TipoDato.INTERO, true);

        assertEquals(primo.hashCode(), secondo.hashCode());
    }

    @Test
    void toString_contieneNomeTipoDatoEObbligatorieta() {
        Campo campo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, true);

        assertEquals("Campo{nome='Nome', tipoDato=STRINGA, obbligatorio=true}", campo.toString());
    }
}
