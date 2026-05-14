package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.catalogo.*;
import it.unibs.ingsoft.persistence.dto.CatalogoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoTest {
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

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
