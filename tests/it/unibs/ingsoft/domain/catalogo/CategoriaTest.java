package it.unibs.ingsoft.domain.catalogo;

import it.unibs.ingsoft.domain.shared.error.DomainException;
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
    void costruttore_conNomeNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class, () -> new Categoria((String) null));

        assertInstanceOf(CatalogFailure.CategoryNameInvalid.class, exception.failure());
    }

    @Test
    void costruttore_conNomeBlank_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class, () -> new Categoria("   "));

        assertInstanceOf(CatalogFailure.CategoryNameInvalid.class, exception.failure());
    }

    @Test
    void costruttoreCopia_conCategoriaEsistente_copiaNomeECampiSpecifici() {
        Categoria originale = new Categoria("Sport");
        originale.addCampoSpecifico(campoSpecifico("Arbitro"));

        Categoria copia = new Categoria(originale);

        assertAll(
                () -> assertEquals("Sport", copia.getNome()),
                () -> assertEquals(originale.getCampiSpecifici(), copia.getCampiSpecifici()),
                () -> assertNotSame(originale.getCampiSpecifici().get(0), copia.getCampiSpecifici().get(0))
        );
    }

    @Test
    void costruttoreCopia_conCategoriaNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Categoria((Categoria) null));
    }

    @Test
    void fromJson_conCampiSpecificiValorizzati_popolaCategoria() {
        Campo arbitro = campoSpecifico("Arbitro");

        Categoria categoria = Categoria.fromJson("Sport", List.of(arbitro));

        assertAll(
                () -> assertEquals("Sport", categoria.getNome()),
                () -> assertEquals(List.of(arbitro), categoria.getCampiSpecifici())
        );
    }

    @Test
    void fromJson_conCampiSpecificiNull_creaCategoriaSenzaCampi() {
        Categoria categoria = Categoria.fromJson("Sport", null);

        assertAll(
                () -> assertEquals("Sport", categoria.getNome()),
                () -> assertTrue(categoria.getCampiSpecifici().isEmpty())
        );
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
    void addCampoSpecifico_conCampoComune_lanciaIllegalStateException() {
        Categoria categoria = new Categoria("Sport");
        Campo comune = new Campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false);

        DomainException exception = assertThrows(DomainException.class, () -> categoria.addCampoSpecifico(comune));

        assertInstanceOf(CatalogFailure.CategoryFieldNotSpecific.class, exception.failure());
    }

    @Test
    void addCampoSpecifico_conNomeDuplicatoCaseInsensitive_lanciaIllegalStateException() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campoSpecifico("Arbitro"));

        DomainException exception = assertThrows(DomainException.class,
                () -> categoria.addCampoSpecifico(campoSpecifico("arbitro")));

        CatalogFailure.CategoryFieldDuplicated failure =
                assertInstanceOf(CatalogFailure.CategoryFieldDuplicated.class, exception.failure());
        assertAll(
                () -> assertEquals("Sport", failure.categoryName()),
                () -> assertEquals("arbitro", failure.fieldName())
        );
    }

    @Test
    void addCampoSpecifico_conCampoNull_lanciaNullPointerException() {
        Categoria categoria = new Categoria("Sport");

        assertThrows(NullPointerException.class, () -> categoria.addCampoSpecifico(null));
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
    void removeCampoSpecifico_conListaNonVuotaENomeAssente_restituisceFalse() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campoSpecifico("Arbitro"));

        assertAll(
                () -> assertFalse(categoria.removeCampoSpecifico("Allenatore")),
                () -> assertEquals(1, categoria.getCampiSpecifici().size())
        );
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
    void setObbligatorietaCampoSpecifico_conListaNonVuotaECampoAssente_restituisceFalseENonModificaCampo() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campoSpecifico("Arbitro"));

        boolean aggiornato = categoria.setObbligatorietaCampoSpecifico("Allenatore", true);

        assertAll(
                () -> assertFalse(aggiornato),
                () -> assertFalse(categoria.getCampiSpecifici().get(0).isObbligatorio())
        );
    }

    @Test
    void getCampiSpecifici_quandoSiModificaListaRestituita_lanciaUnsupportedOperationException() {
        Categoria categoria = new Categoria("Sport");

        assertThrows(UnsupportedOperationException.class,
                () -> categoria.getCampiSpecifici().add(campoSpecifico("Arbitro")));
    }

    @Test
    void equals_conStessaIstanza_restituisceTrue() {
        Categoria categoria = new Categoria("Sport");

        assertEquals(categoria, categoria);
    }

    @Test
    void equals_conNomeUgualeCaseInsensitive_restituisceTrue() {
        Categoria categoria = new Categoria("Sport");

        assertEquals(categoria, new Categoria("sport"));
    }

    @Test
    void equals_conTipoDiverso_restituisceFalse() {
        Categoria categoria = new Categoria("Sport");

        assertNotEquals(categoria, "Sport");
    }

    @Test
    void equals_conNull_restituisceFalse() {
        Categoria categoria = new Categoria("Sport");

        assertNotEquals(null, categoria);
    }

    @Test
    void equals_conNomeDiverso_restituisceFalse() {
        Categoria categoria = new Categoria("Sport");

        assertNotEquals(categoria, new Categoria("Teatro"));
    }

    @Test
    void hashCode_conNomeUgualeCaseInsensitive_restituisceStessoHashCode() {
        Categoria categoria = new Categoria("Sport");

        assertEquals(categoria.hashCode(), new Categoria("sport").hashCode());
    }

    @Test
    void toString_restituisceNomeCategoria() {
        Categoria categoria = new Categoria("Sport");

        assertEquals("Sport", categoria.toString());
    }

    private Campo campoSpecifico(String nome) {
        return new Campo(nome, TipoCampo.SPECIFICO, TipoDato.STRINGA, false);
    }
}
