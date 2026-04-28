package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.testsupport.InMemoryCatalogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceTest {
    private InMemoryCatalogoRepository repo;
    private CatalogoService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCatalogoRepository();
        service = new CatalogoService(repo);
    }

    @Test
    void UC03_initiateCampiBase_success_persistsEightBaseFields() {
        service.initiateCampiBase();

        assertEquals(8, service.getCampiBase().size());
        assertTrue(repo.get().isCampiBaseFissati());
        assertEquals(1, repo.saveCount());
    }

    @Test
    void UC03_addCampiBaseConExtra_success_persistsFixedAndExtraFields() {
        service.addCampiBaseConExtra(List.of("Codice evento"), List.of(TipoDato.STRINGA));

        assertEquals(9, service.getCampiBase().size());
        assertTrue(service.getCampiBase().stream().anyMatch(c -> c.getNome().equals("Codice evento")));
        assertEquals(1, repo.saveCount());
    }

    @Test
    void UC03_addCampiBaseConExtra_duplicateFixedName_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampiBaseConExtra(List.of(AppConstants.CAMPO_TITOLO), List.of(TipoDato.STRINGA)));
        assertEquals(0, repo.saveCount());
    }

    @Test
    void UC05_addCampoComune_success_persistsCatalog() {
        service.initiateCampiBase();

        service.addCampoComune("Difficolta", TipoDato.STRINGA, false);

        assertEquals(1, service.getCampiComuni().size());
        assertEquals("Difficolta", service.getCampiComuni().get(0).getNome());
        assertEquals(2, repo.saveCount());
    }

    @Test
    void UC05_AggiungiCampo_duplicateOrBlankName_rejected() {
        service.initiateCampiBase();
        service.addCampoComune("Difficolta", TipoDato.STRINGA, false);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.addCampoComune("Difficolta", TipoDato.STRINGA, false)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.addCampoComune(" ", TipoDato.STRINGA, false))
        );
    }

    @Test
    void UC06_removeCampoComune_successAndMissingReturnsFalse() {
        service.addCampoComune("Difficolta", TipoDato.STRINGA, false);

        assertTrue(service.removeCampoComune("difficolta"));
        assertFalse(service.removeCampoComune("missing"));
        assertTrue(service.getCampiComuni().isEmpty());
        assertEquals(2, repo.saveCount());
    }

    @Test
    void UC07_modificaObbligatorietaCampoComune_successAndMissingReturnsFalse() {
        service.addCampoComune("Difficolta", TipoDato.STRINGA, false);

        assertTrue(service.setObbligatorietaCampoComune("Difficolta", true));
        assertTrue(service.getCampiComuni().get(0).isObbligatorio());
        assertFalse(service.setObbligatorietaCampoComune("missing", true));
        assertEquals(2, repo.saveCount());
    }

    @Test
    void UC08_createCategoria_successAndDuplicateRejected() {
        Categoria categoria = service.createCategoria("Escursione");

        assertEquals("Escursione", categoria.getNome());
        assertEquals(1, service.getCategorie().size());
        assertThrows(IllegalArgumentException.class, () -> service.createCategoria("escursione"));
        assertEquals(1, repo.saveCount());
    }

    @Test
    void UC09_removeCategoria_removesCategoryAndSpecificFields() {
        service.createCategoria("Escursione");
        service.addCampoSpecifico("Escursione", "Guida", TipoDato.STRINGA, false);

        assertTrue(service.removeCategoria("escursione"));

        assertTrue(service.getCategorie().isEmpty());
        assertEquals(3, repo.saveCount());
    }

    @Test
    void UC10_addCampoSpecifico_successAndMissingCategoryRejected() {
        service.createCategoria("Escursione");

        service.addCampoSpecifico("Escursione", "Guida", TipoDato.STRINGA, false);

        assertEquals(1, service.getCategorie().get(0).getCampiSpecifici().size());
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoSpecifico("Missing", "Altro", TipoDato.STRINGA, false));
    }

    @Test
    void UC11_removeCampoSpecifico_successAndMissingReturnsFalse() {
        service.createCategoria("Escursione");
        service.addCampoSpecifico("Escursione", "Guida", TipoDato.STRINGA, false);

        assertTrue(service.removeCampoSpecifico("Escursione", "Guida"));
        assertFalse(service.removeCampoSpecifico("Escursione", "Missing"));
        assertTrue(service.getCategorie().get(0).getCampiSpecifici().isEmpty());
    }

    @Test
    void UC12_modificaObbligatorietaCampoSpecifico_successAndMissingReturnsFalse() {
        service.createCategoria("Escursione");
        service.addCampoSpecifico("Escursione", "Guida", TipoDato.STRINGA, false);

        assertTrue(service.setObbligatorietaCampoSpecifico("Escursione", "Guida", true));
        assertTrue(service.getCategorie().get(0).getCampiSpecifici().get(0).isObbligatorio());
        assertFalse(service.setObbligatorietaCampoSpecifico("Escursione", "Missing", false));
    }
}
