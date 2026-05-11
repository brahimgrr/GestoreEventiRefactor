package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaTest {
    @Test
    void costruttore_conNomeValidoTrimmato_salvaNomeSenzaSpaziEsterni() {
        Categoria categoria = new Categoria("  Sport  ");

        assertEquals("Sport", categoria.getNome());
    }

    @Test
    void costruttore_conNomeNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Categoria((String) null));
    }

    @Test
    void costruttore_conNomeBlank_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Categoria("   "));
    }

    @Test
    void addCampoSpecifico_conCampiFuoriOrdine_liOrdinaAlfabeticamenteIgnorandoMaiuscole() {
        Categoria categoria = new Categoria("Sport");

        categoria.addCampoSpecifico(campoSpecifico("zaino"));
        categoria.addCampoSpecifico(campoSpecifico("Arbitro"));

        assertEquals(List.of("Arbitro", "zaino"),
                categoria.getCampiSpecifici().stream().map(Campo::getNome).toList());
    }

    @Test
    void addCampoSpecifico_conCampoComune_lanciaIllegalArgumentException() {
        Categoria categoria = new Categoria("Sport");
        Campo comune = new Campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false);

        assertThrows(IllegalArgumentException.class, () -> categoria.addCampoSpecifico(comune));
    }

    @Test
    void addCampoSpecifico_conNomeDuplicatoCaseInsensitive_lanciaIllegalArgumentException() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campoSpecifico("Arbitro"));

        assertThrows(IllegalArgumentException.class, () -> categoria.addCampoSpecifico(campoSpecifico("arbitro")));
    }

    @Test
    void removeCampoSpecifico_conNomePresenteRimuoveSoloQuelCampo_restituisceTrue() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campoSpecifico("Arbitro"));

        boolean rimosso = categoria.removeCampoSpecifico("arbitro");

        assertAll(
                () -> assertTrue(rimosso),
                () -> assertTrue(categoria.getCampiSpecifici().isEmpty())
        );
    }

    @Test
    void removeCampoSpecifico_conNomeAssente_restituisceFalse() {
        Categoria categoria = new Categoria("Sport");

        assertFalse(categoria.removeCampoSpecifico("Arbitro"));
    }

    @Test
    void setObbligatorietaCampoSpecifico_conCampoPresenteAggiornaFlag_restituisceTrue() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(new Campo("Arbitro", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false));

        boolean aggiornato = categoria.setObbligatorietaCampoSpecifico("arbitro", true);

        assertAll(
                () -> assertTrue(aggiornato),
                () -> assertTrue(categoria.getCampiSpecifici().get(0).isObbligatorio())
        );
    }

    @Test
    void setObbligatorietaCampoSpecifico_conCampoAssente_restituisceFalse() {
        Categoria categoria = new Categoria("Sport");

        assertFalse(categoria.setObbligatorietaCampoSpecifico("Arbitro", true));
    }

    @Test
    void getCampiSpecifici_quandoSiModificaListaRestituita_lanciaUnsupportedOperationException() {
        Categoria categoria = new Categoria("Sport");

        assertThrows(UnsupportedOperationException.class,
                () -> categoria.getCampiSpecifici().add(campoSpecifico("Arbitro")));
    }

    private Campo campoSpecifico(String nome) {
        return new Campo(nome, TipoCampo.SPECIFICO, TipoDato.STRINGA, false);
    }
}
