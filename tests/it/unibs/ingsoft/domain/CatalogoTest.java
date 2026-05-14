package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.catalogo.*;
import it.unibs.ingsoft.persistence.dto.CatalogoDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoTest {
    @Test
    void fromJson_conListeValorizzateECampiBaseFissati_popolaCatalogo() {
        Campo base = campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        Campo comune = campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false);
        Categoria categoria = new Categoria("Sport");

        Catalogo catalogo = Catalogo.fromJson(
                List.of(base),
                true,
                List.of(comune),
                List.of(categoria)
        );

        assertAll(
                () -> assertTrue(catalogo.isCampiBaseFissati()),
                () -> assertEquals(List.of(base), catalogo.getCampiBase()),
                () -> assertEquals(List.of(comune), catalogo.getCampiComuni()),
                () -> assertEquals(List.of(categoria), catalogo.getCategorie())
        );
    }

    @Test
    void fromJson_conListeNullECampiBaseNonFissati_creaCatalogoVuoto() {
        Catalogo catalogo = Catalogo.fromJson(null, false, null, null);

        assertAll(
                () -> assertFalse(catalogo.isCampiBaseFissati()),
                () -> assertTrue(catalogo.getCampiBase().isEmpty()),
                () -> assertTrue(catalogo.getCampiComuni().isEmpty()),
                () -> assertTrue(catalogo.getCategorie().isEmpty())
        );
    }

    @Test
    void fissareCampiBase_conListaValida_salvaCampiEImpostaFlagFissati() {
        CatalogoDTO catalogo = new CatalogoDTO();

        Campo base = campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        catalogo.fissareCampiBase(List.of(base), null);

        assertAll(
                () -> assertTrue(catalogo.isCampiBaseFissati()),
                () -> assertEquals(List.of(base), catalogo.getCampiBase())
        );
    }

    @Test
    void fissareCampiBase_conExtraValido_aggiungeCampiExtra() {
        Catalogo catalogo = new Catalogo();

        Campo base = campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        Campo extra = campo("Descrizione", TipoCampo.BASE, TipoDato.STRINGA, false);
        catalogo.fissareCampiBase(List.of(base), List.of(extra));

        assertEquals(List.of(base, extra), catalogo.getCampiBase());
    }

    @Test
    void fissareCampiBase_conExtraNullo_ignoraElementoNull() {
        Catalogo catalogo = new Catalogo();

        Campo base = campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        Campo extra = campo("Descrizione", TipoCampo.BASE, TipoDato.STRINGA, false);
        List<Campo> extraConNull = new ArrayList<>();
        extraConNull.add(null);
        extraConNull.add(extra);

        catalogo.fissareCampiBase(List.of(base), extraConNull);

        assertEquals(List.of(base, extra), catalogo.getCampiBase());
    }

    @Test
    void fissareCampiBase_conExtraGiaPresenteNeiCampiComuni_lanciaIllegalStateException() {
        Catalogo catalogo = new Catalogo();
        catalogo.addCampoComune(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false));

        assertThrows(IllegalStateException.class,
                () -> catalogo.fissareCampiBase(
                        List.of(campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true)),
                        List.of(campo("eta", TipoCampo.BASE, TipoDato.INTERO, false))
                ));
    }

    @Test
    void fissareCampiBase_conDuplicatoCaseInsensitive_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();

        assertThrows(DomainException.class,
                () -> catalogo.fissareCampiBase(List.of(
                        campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true),
                        campo("titolo", TipoCampo.BASE, TipoDato.STRINGA, true)
                        ), null));
    }

    @Test
    void fissareCampiBase_quandoGiaFissati_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.fissareCampiBase(List.of(campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true)), null);

        assertThrows(DomainException.class,
                () -> catalogo.fissareCampiBase(List.of(campo("Data", TipoCampo.BASE, TipoDato.DATA, true)), null));
    }

    @Test
    void addCampoComune_conNomeNuovo_aggiungeCampoComune() {
        CatalogoDTO catalogo = new CatalogoDTO();
        Campo campo = campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false);

        catalogo.addCampoComune(campo);

        assertEquals(List.of(campo), catalogo.getCampiComuni());
    }

    @Test
    void addCampoComune_conNomeGiaPresente_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.fissareCampiBase(List.of(campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true)), null);

        assertThrows(DomainException.class,
                () -> catalogo.addCampoComune(campo("titolo", TipoCampo.COMUNE, TipoDato.STRINGA, false)));
    }

    @Test
    void removeCampoComune_conNomePresenteRimuoveCampo_restituisceTrue() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.addCampoComune(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false));

        boolean rimosso = catalogo.removeCampoComune("eta");

        assertAll(
                () -> assertTrue(rimosso),
                () -> assertTrue(catalogo.getCampiComuni().isEmpty())
        );
    }

    @Test
    void updateCampoComune_conNomePresenteCambiaObbligatorieta_restituisceTrue() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.addCampoComune(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false));

        boolean aggiornato = catalogo.updateCampoComune("eta", true);

        assertAll(
                () -> assertTrue(aggiornato),
                () -> assertTrue(catalogo.getCampiComuni().get(0).isObbligatorio())
        );
    }

    @Test
    void updateCampoComune_conNomeAssente_restituisceFalse() {
        CatalogoDTO catalogo = new CatalogoDTO();

        assertFalse(catalogo.updateCampoComune("Eta", true));
    }

    @Test
    void updateCampoComune_conListaNonVuotaENomeAssente_restituisceFalseENonModificaCampo() {
        Catalogo catalogo = new Catalogo();
        catalogo.addCampoComune(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false));

        boolean aggiornato = catalogo.updateCampoComune("Note", true);

        assertAll(
                () -> assertFalse(aggiornato),
                () -> assertFalse(catalogo.getCampiComuni().get(0).isObbligatorio())
        );
    }

    @Test
    void addCategoria_conNomeNuovo_aggiungeCategoriaERestituisceIstanzaAggiunta() {
        CatalogoDTO catalogo = new CatalogoDTO();

        Categoria categoria = catalogo.addCategoria("Sport");

        assertAll(
                () -> assertEquals("Sport", categoria.getNome()),
                () -> assertEquals(List.of(categoria), catalogo.getCategorie())
        );
    }

    @Test
    void addCategoria_conNomeDuplicatoCaseInsensitive_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.addCategoria("Sport");

        assertThrows(DomainException.class, () -> catalogo.addCategoria("sport"));
    }

    @Test
    void removeCategoria_conNomePresenteRimuoveCategoria_restituisceTrue() {
        Catalogo catalogo = new Catalogo();
        catalogo.addCategoria("Sport");

        boolean rimossa = catalogo.removeCategoria("sport");

        assertAll(
                () -> assertTrue(rimossa),
                () -> assertTrue(catalogo.getCategorie().isEmpty())
        );
    }

    @Test
    void removeCategoria_conNomeAssente_restituisceFalse() {
        Catalogo catalogo = new Catalogo();
        catalogo.addCategoria("Sport");

        boolean rimossa = catalogo.removeCategoria("Teatro");

        assertAll(
                () -> assertFalse(rimossa),
                () -> assertEquals(1, catalogo.getCategorie().size())
        );
    }

    @Test
    void getCategoriaOrThrow_conCategoriaAssente_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();

        assertThrows(DomainException.class, () -> catalogo.getCategoriaOrThrow("Sport"));
    }

    @Test
    void addCampoSpecifico_conCategoriaEsistente_aggiungeCampoAllaCategoriaIndicata() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.addCategoria("Sport");

        Campo specifico = campo("Arbitro", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false);
        catalogo.addCampoSpecifico("sport", specifico);

        assertEquals(List.of(specifico), catalogo.getCategoriaOrThrow("Sport").getCampiSpecifici());
    }

    @Test
    void addCampoSpecifico_conNomeGiaPresenteNeiCampiComuni_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();
        catalogo.addCategoria("Sport");
        catalogo.addCampoComune(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false));

        assertThrows(DomainException.class,
                () -> catalogo.addCampoSpecifico("Sport", campo("eta", TipoCampo.SPECIFICO, TipoDato.INTERO, false)));
    }

    @Test
    void removeCampoSpecifico_conCategoriaAssente_lanciaIllegalStateException() {
        CatalogoDTO catalogo = new CatalogoDTO();

        assertThrows(DomainException.class, () -> catalogo.removeCampoSpecifico("Sport", "Eta"));
    }

    @Test
    void updateCampoSpecifico_conCampoPresenteAggiornaObbligatorieta_restituisceTrue() {
        Catalogo catalogo = new Catalogo();
        catalogo.addCategoria("Sport");
        catalogo.addCampoSpecifico("Sport", campo("Arbitro", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false));

        boolean aggiornato = catalogo.updateCampoSpecifico("sport", "arbitro", true);

        assertAll(
                () -> assertTrue(aggiornato),
                () -> assertTrue(catalogo.getCategoriaOrThrow("Sport").getCampiSpecifici().get(0).isObbligatorio())
        );
    }

    @Test
    void updateCampoSpecifico_conCampoAssente_restituisceFalseENonModificaCampo() {
        Catalogo catalogo = new Catalogo();
        catalogo.addCategoria("Sport");
        catalogo.addCampoSpecifico("Sport", campo("Arbitro", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false));

        boolean aggiornato = catalogo.updateCampoSpecifico("Sport", "Allenatore", true);

        assertAll(
                () -> assertFalse(aggiornato),
                () -> assertFalse(catalogo.getCategoriaOrThrow("Sport").getCampiSpecifici().get(0).isObbligatorio())
        );
    }

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
