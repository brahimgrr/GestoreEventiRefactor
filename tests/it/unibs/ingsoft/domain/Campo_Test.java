package it.unibs.ingsoft.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Campo_Test {
    @Test
    void costruttore_conNomeValidoTrimmato_salvaNomeSenzaSpaziEsterni() {
        Campo campo = new Campo("  Nome  ", TipoCampo.COMUNE, TipoDato.STRINGA, true);

        assertEquals("Nome", campo.getNome());
    }

    @Test
    void costruttore_conNomeNull_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> new Campo(null, TipoCampo.COMUNE, TipoDato.STRINGA, true));
    }

    @Test
    void costruttore_conNomeBlank_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> new Campo("   ", TipoCampo.COMUNE, TipoDato.STRINGA, true));
    }

    @Test
    void costruttore_conTipoNull_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> new Campo("Nome", null, TipoDato.STRINGA, true));
    }

    @Test
    void costruttore_conTipoDatoNull_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> new Campo("Nome", TipoCampo.COMUNE, null, true));
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

    /*
    Ha senso fare questo Test? mostra che equals non fa tutto il lavoro
    ma che parte del lavoro è delegata ad altre parti del programma
     */
    @Test
    void equals_conStessoNomeInMaiuscoleDiverse_restituisceTrue() {
        Campo primo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);
        Campo secondo = new Campo("nome", TipoCampo.SPECIFICO, TipoDato.INTERO, true);

        assertEquals(primo, secondo);
    }

    @Test
    void hashCode_conStessoNomeInMaiuscoleDiverse_restituisceStessoHashCode() {
        Campo primo = new Campo("Nome", TipoCampo.COMUNE, TipoDato.STRINGA, false);
        Campo secondo = new Campo("nome", TipoCampo.SPECIFICO, TipoDato.INTERO, true);

        assertEquals(primo.hashCode(), secondo.hashCode());
    }
}
