package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.factory.CampoFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceTest {
    @Test
    void costruttori_conDipendenzeNull_lancianoNullPointerException() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CampoCatalogo_Service campoService = new CampoCatalogo_Service(repo, CampoFactory.getInstance());
        CategoriaCatalogoService categoriaService = new CategoriaCatalogoService(repo);

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new Catalogo_Service((it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository) null)),
                () -> assertThrows(NullPointerException.class, () -> new Catalogo_Service(repo, null)),
                () -> assertThrows(NullPointerException.class, () -> new Catalogo_Service(null, categoriaService)),
                () -> assertThrows(NullPointerException.class, () -> new Catalogo_Service(campoService, null)),
                () -> assertThrows(NullPointerException.class, () -> new CampoCatalogo_Service(null, CampoFactory.getInstance())),
                () -> assertThrows(NullPointerException.class, () -> new CampoCatalogo_Service(repo, null)),
                () -> assertThrows(NullPointerException.class, () -> new CategoriaCatalogoService(null))
        );
    }

    @Test
    void configuraCampiBase_conCampoExtra_persisteCampiBaseFissiEdExtra() {
        Catalogo_Service service = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());

        service.configuraCampiBase(List.of(new CampoBaseExtraRequest("Equipaggiamento", TipoDato.STRINGA)));

        assertAll(
                () -> assertFalse(service.isPrimaConfigurazioneNecessaria()),
                () -> assertEquals(9, service.getCampiBase().size()),
                () -> assertTrue(service.getCampiBase().stream()
                        .anyMatch(campo -> campo.getNome().equals("Equipaggiamento")))
        );
    }

    @Test
    void configuraCampiBase_conListaNull_oVuota_inizializzaCampiBasePredefiniti() {
        Catalogo_Service serviceConNull = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        Catalogo_Service serviceConVuota = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());

        serviceConNull.configuraCampiBase(null);
        serviceConVuota.configuraCampiBase(List.of());

        assertAll(
                () -> assertEquals(8, serviceConNull.getCampiBase().size()),
                () -> assertEquals(8, serviceConVuota.getCampiBase().size()),
                () -> assertFalse(serviceConNull.isPrimaConfigurazioneNecessaria())
        );
    }

    @Test
    void configuraCampiBase_conExtraInvalido_inizializzaFallbackERilancia() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        Catalogo_Service service = new Catalogo_Service(repo);

        assertAll(
                () -> assertThrows(DomainException.class,
                        () -> service.configuraCampiBase(List.of(new CampoBaseExtraRequest("Titolo", TipoDato.STRINGA)))),
                () -> assertFalse(repo.load().getCampiBase().isEmpty())
        );
    }

    @Test
    void metodiCompatibilita_copronoCampiBasePredefinitiENomeEsistente() {
        Catalogo_Service service = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());

        service.initiateCampiBase();

        assertAll(
                () -> assertEquals(8, service.getCampiBasePredefiniti().size()),
                () -> assertTrue(service.nomeEsistente("Titolo")),
                () -> assertFalse(service.nomeEsistente("Campo inesistente"))
        );
    }

    @Test
    void addCampiBaseConExtra_conDatiValidi_aggiungeCampoExtra() {
        Catalogo_Service service = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());

        service.addCampiBaseConExtra(List.of("Equipaggiamento"), List.of(TipoDato.STRINGA));

        assertTrue(service.getCampiBase().stream()
                .anyMatch(campo -> campo.getNome().equals("Equipaggiamento")));
    }

    @Test
    void createCategoria_eAddCampoSpecifico_colleganoCampoAllaCategoriaPersistita() {
        Catalogo_Service service = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        service.configuraCampiBase(List.of());

        Categoria categoria = service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertAll(
                () -> assertEquals("Sport", categoria.getNome()),
                () -> assertEquals(1, service.getCategorie().size()),
                () -> assertEquals("Arbitro", service.getCategorie().get(0).getCampiSpecifici().get(0).getNome())
        );
    }

    @Test
    void createCategoriaDuplicata_lanciaEccezioneDominio() {
        Catalogo_Service service = serviceVuoto();
        service.createCategoria("Sport");

        assertThrows(DomainException.class, () -> service.createCategoria("sport"));
    }

    @Test
    void addCampoComune_eSetObbligatorietaCampoComune_aggiornanoCampoComunePersistito() {
        Catalogo_Service service = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        service.configuraCampiBase(List.of());
        service.addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));

        CatalogoOperationResult result = service.setObbligatorietaCampoComune(
                new CampoObbligatorietaRequest("Note", true));

        assertAll(
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, result),
                () -> assertTrue(service.getCampiComuni().get(0).isObbligatorio())
        );
    }

    @Test
    void addCampoComune_conRequestNull_lanciaNullPointerException() {
        Catalogo_Service service = serviceVuoto();

        assertThrows(NullPointerException.class, () -> service.addCampoComune(null));
    }

    @Test
    void rimuoviCampoComune_restituisceSuccessoONonTrovato() {
        Catalogo_Service service = serviceVuoto();
        service.addCampoComune("Note", TipoDato.STRINGA, false);

        assertAll(
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, service.rimuoviCampoComune("note")),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, service.rimuoviCampoComune("note"))
        );
    }

    @Test
    void setObbligatorietaCampoComune_restituisceNessunaModificaONonTrovato() {
        Catalogo_Service service = serviceVuoto();
        service.addCampoComune("Note", TipoDato.STRINGA, false);

        assertAll(
                () -> assertEquals(CatalogoOperationResult.NESSUNA_MODIFICA,
                        service.setObbligatorietaCampoComune(new CampoObbligatorietaRequest("note", false))),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO,
                        service.setObbligatorietaCampoComune(new CampoObbligatorietaRequest("Assente", true))),
                () -> assertThrows(NullPointerException.class,
                        () -> service.setObbligatorietaCampoComune(null))
        );
    }

    @Test
    void rimuoviCategoria_restituisceSuccessoONonTrovato() {
        Catalogo_Service service = serviceVuoto();
        service.createCategoria("Sport");

        assertAll(
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, service.rimuoviCategoria("sport")),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, service.rimuoviCategoria("sport"))
        );
    }

    @Test
    void rimuoviCampoSpecifico_restituisceSuccessoONonTrovato() {
        Catalogo_Service service = serviceVuoto();
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Arbitro", TipoDato.BOOLEANO, false);

        assertAll(
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, service.rimuoviCampoSpecifico("sport", "arbitro")),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, service.rimuoviCampoSpecifico("sport", "arbitro"))
        );
    }

    @Test
    void addCampoSpecifico_conRequestNull_lanciaNullPointerException() {
        Catalogo_Service service = serviceVuoto();
        service.createCategoria("Sport");

        assertThrows(NullPointerException.class, () -> service.addCampoSpecifico("Sport", null));
    }

    @Test
    void setObbligatorietaCampoSpecifico_restituisceTuttiGliEsiti() {
        Catalogo_Service service = serviceVuoto();
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Arbitro", TipoDato.BOOLEANO, false);

        assertAll(
                () -> assertEquals(CatalogoOperationResult.NESSUNA_MODIFICA,
                        service.setObbligatorietaCampoSpecifico("sport", new CampoObbligatorietaRequest("arbitro", false))),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO,
                        service.setObbligatorietaCampoSpecifico("sport", new CampoObbligatorietaRequest("arbitro", true))),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO,
                        service.setObbligatorietaCampoSpecifico("sport", new CampoObbligatorietaRequest("Assente", true))),
                () -> assertThrows(NullPointerException.class,
                        () -> service.setObbligatorietaCampoSpecifico("sport", null))
        );
    }

    private Catalogo_Service serviceVuoto() {
        Catalogo_Service service = new Catalogo_Service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        service.configuraCampiBase(List.of());
        return service;
    }
}
