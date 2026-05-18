package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.CampoFactory;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampoCatalogoServiceTest {
    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new CampoCatalogoService(null, CampoFactory.getInstance())),
                () -> assertThrows(NullPointerException.class,
                        () -> new CampoCatalogoService(repo, null))
        );
    }

    @Test
    void campiBase_conPrimaConfigurazioneENomi_copreQueryEInizializzazione() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CampoCatalogoService service = service(repo);

        assertTrue(service.isPrimaConfigurazioneNecessaria());
        service.configuraCampiBase(null);

        assertAll(
                () -> assertFalse(service.isPrimaConfigurazioneNecessaria()),
                () -> assertEquals(8, service.getCampiBase().size()),
                () -> assertEquals(8, service.getCampiBasePredefiniti().size()),
                () -> assertTrue(service.nomeEsistente("Titolo")),
                () -> assertFalse(service.nomeEsistente("Assente"))
        );
    }

    @Test
    void configuraCampiBase_conListaVuotaOExtraValido_fissaCampiBase() {
        CampoCatalogoService vuota = service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        CampoCatalogoService extra = service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());

        vuota.configuraCampiBase(List.of());
        extra.configuraCampiBase(List.of(new CampoBaseExtraRequest("Equipaggiamento", TipoDato.STRINGA)));

        assertAll(
                () -> assertEquals(8, vuota.getCampiBase().size()),
                () -> assertEquals(9, extra.getCampiBase().size()),
                () -> assertTrue(extra.getCampiBase().stream()
                        .anyMatch(campo -> campo.getNome().equals("Equipaggiamento")))
        );
    }

    @Test
    void configuraCampiBase_conExtraInvalido_fissaFallbackSoloSeNonGiaFissato() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repoNonFissato =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repoGiaFissato =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CampoCatalogoService nonFissato = service(repoNonFissato);
        CampoCatalogoService giaFissato = service(repoGiaFissato);
        giaFissato.initiateCampiBase();

        assertAll(
                () -> assertThrows(DomainException.class,
                        () -> nonFissato.configuraCampiBase(List.of(new CampoBaseExtraRequest("Titolo", TipoDato.STRINGA)))),
                () -> assertEquals(8, repoNonFissato.load().getCampiBase().size()),
                () -> assertThrows(DomainException.class,
                        () -> giaFissato.configuraCampiBase(List.of(new CampoBaseExtraRequest("Extra", TipoDato.STRINGA)))),
                () -> assertEquals(8, repoGiaFissato.load().getCampiBase().size())
        );
    }

    @Test
    void addCampiBaseConExtra_conDatiValidiOInvalidi_copreEsitiFactory() {
        CampoCatalogoService valido = service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        CampoCatalogoService nullList = service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        CampoCatalogoService mismatch = service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());

        valido.addCampiBaseConExtra(List.of("Extra"), List.of(TipoDato.STRINGA));

        assertAll(
                () -> assertTrue(valido.getCampiBase().stream().anyMatch(c -> c.getNome().equals("Extra"))),
                () -> assertThrows(DomainException.class,
                        () -> nullList.addCampiBaseConExtra(null, List.of(TipoDato.STRINGA))),
                () -> assertThrows(DomainException.class,
                        () -> mismatch.addCampiBaseConExtra(List.of("A", "B"), List.of(TipoDato.STRINGA)))
        );
    }

    @Test
    void campiComuni_copreAggiuntaRimozioneObbligatorietaEQuery() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CampoCatalogoService service = service(repo);

        service.addCampoComune("Note", TipoDato.STRINGA, false);
        Campo campo = service.getCampiComuni().get(0);

        assertAll(
                () -> assertEquals("Note", campo.getNome()),
                () -> assertTrue(service.setObbligatorietaCampoComune("note", true)),
                () -> assertFalse(service.setObbligatorietaCampoComune("assente", true)),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO,
                        service.setObbligatorietaCampoComune(new CampoObbligatorietaRequest("note", false))),
                () -> assertEquals(CatalogoOperationResult.NESSUNA_MODIFICA,
                        service.setObbligatorietaCampoComune(new CampoObbligatorietaRequest("note", false))),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO,
                        service.setObbligatorietaCampoComune(new CampoObbligatorietaRequest("assente", true))),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, service.rimuoviCampoComune("note")),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, service.rimuoviCampoComune("note"))
        );
    }

    @Test
    void campiComuni_conRequestNullODuplicato_lanciaEccezioni() {
        CampoCatalogoService service = service(new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        service.addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> service.addCampoComune(null)),
                () -> assertThrows(NullPointerException.class, () -> service.setObbligatorietaCampoComune(null)),
                () -> assertThrows(DomainException.class,
                        () -> service.addCampoComune("note", TipoDato.STRINGA, false))
        );
    }

    @Test
    void campiSpecifici_copreAggiuntaRimozioneObbligatorietaEQuery() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CategoriaCatalogoService categoriaService = new CategoriaCatalogoService(repo);
        CampoCatalogoService service = service(repo);
        categoriaService.createCategoria("Sport");
        service.addCampoSpecifico("Sport", new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertAll(
                () -> assertTrue(service.setObbligatorietaCampoSpecifico("sport", "arbitro", true)),
                () -> assertFalse(service.setObbligatorietaCampoSpecifico("sport", "assente", true)),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO,
                        service.setObbligatorietaCampoSpecifico("sport", new CampoObbligatorietaRequest("arbitro", false))),
                () -> assertEquals(CatalogoOperationResult.NESSUNA_MODIFICA,
                        service.setObbligatorietaCampoSpecifico("sport", new CampoObbligatorietaRequest("arbitro", false))),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO,
                        service.setObbligatorietaCampoSpecifico("sport", new CampoObbligatorietaRequest("assente", true))),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO,
                        service.rimuoviCampoSpecifico("sport", "arbitro")),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO,
                        service.rimuoviCampoSpecifico("sport", "arbitro"))
        );
    }

    @Test
    void campiSpecifici_conRequestNullCategoriaAssenteODuplicato_lanciaEccezioni() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CategoriaCatalogoService categoriaService = new CategoriaCatalogoService(repo);
        CampoCatalogoService service = service(repo);
        categoriaService.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Arbitro", TipoDato.BOOLEANO, false);

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> service.addCampoSpecifico("Sport", null)),
                () -> assertThrows(NullPointerException.class,
                        () -> service.setObbligatorietaCampoSpecifico("Sport", null)),
                () -> assertThrows(DomainException.class,
                        () -> service.addCampoSpecifico("Sport", "arbitro", TipoDato.BOOLEANO, false)),
                () -> assertThrows(DomainException.class,
                        () -> service.addCampoSpecifico("Teatro", "Regista", TipoDato.STRINGA, false)),
                () -> assertThrows(DomainException.class,
                        () -> service.removeCampoSpecifico("Teatro", "Regista")),
                () -> assertThrows(DomainException.class,
                        () -> service.setObbligatorietaCampoSpecifico("Teatro", "Regista", true)),
                () -> assertThrows(DomainException.class,
                        () -> service.setObbligatorietaCampoSpecifico("Teatro",
                                new CampoObbligatorietaRequest("Regista", true)))
        );
    }

    private CampoCatalogoService service(ApplicationIntegrationSupport.InMemoryCatalogoRepository repo) {
        return new CampoCatalogoService(repo, CampoFactory.getInstance());
    }
}
