package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.campo.ICampoConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.categoria.ICategoriaConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu.IConfiguratoreView;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratoreControllerTest {
    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ConfiguratoreFixture fixture = fixture(true, new MockConfiguratoreView());

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new ConfiguratoreController(null, fixture.service)),
                () -> assertThrows(NullPointerException.class,
                        () -> new ConfiguratoreController(fixture.view, null))
        );
    }

    @Test
    void run_conPrimaConfigurazioneEVisualizzazione_configuraCatalogoPoiLogout() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.VISUALIZZA,
                IConfiguratoreView.MainAction.LOGOUT);
        ConfiguratoreFixture fixture = fixture(false, view);

        fixture.controller.run();

        assertAll(
                () -> assertEquals(1, view.primaConfigurazioneRichiesta),
                () -> assertEquals(1, view.catalogoMostrato),
                () -> assertFalse(fixture.service.isPrimaConfigurazioneNecessaria())
        );
    }

    @Test
    void run_gestisceCampiComuni_aggiungeCambiaObbligatorietaERimuove() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CAMPI_COMUNI,
                IConfiguratoreView.MainAction.LOGOUT);
        view.commonFieldActions.addAll(List.of(
                ICampoConfigView.FieldAction.AGGIUNGI,
                ICampoConfigView.FieldAction.CAMBIA_OBBLIGATORIETA,
                ICampoConfigView.FieldAction.RIMUOVI,
                ICampoConfigView.FieldAction.TORNA));
        ConfiguratoreFixture fixture = fixture(true, view);

        fixture.controller.run();

        assertAll(
                () -> assertEquals(3, view.esitiCatalogo),
                () -> assertTrue(fixture.service.getCampiComuni().isEmpty())
        );
    }

    @Test
    void run_gestisceCategorieECampiSpecifici_applicaLeOperazioniRichieste() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CATEGORIE,
                IConfiguratoreView.MainAction.LOGOUT);
        view.categoryActions.addAll(List.of(
                ICategoriaConfigView.CategoryAction.CREA,
                ICategoriaConfigView.CategoryAction.CAMPI_SPECIFICI,
                ICategoriaConfigView.CategoryAction.RIMUOVI,
                ICategoriaConfigView.CategoryAction.TORNA));
        view.specificFieldActions.addAll(List.of(
                ICampoConfigView.FieldAction.AGGIUNGI,
                ICampoConfigView.FieldAction.CAMBIA_OBBLIGATORIETA,
                ICampoConfigView.FieldAction.RIMUOVI,
                ICampoConfigView.FieldAction.TORNA));
        ConfiguratoreFixture fixture = fixture(true, view);

        fixture.controller.run();

        assertAll(
                () -> assertEquals(5, view.esitiCatalogo),
                () -> assertTrue(fixture.service.getCategorie().isEmpty())
        );
    }

    @Test
    void run_conInputCategoriaVuoti_nonModificaCatalogo() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CATEGORIE,
                IConfiguratoreView.MainAction.LOGOUT);
        view.nomeCategoria = java.util.Optional.empty();
        view.categoryActions.addAll(List.of(
                ICategoriaConfigView.CategoryAction.CREA,
                ICategoriaConfigView.CategoryAction.RIMUOVI,
                ICategoriaConfigView.CategoryAction.CAMPI_SPECIFICI,
                ICategoriaConfigView.CategoryAction.TORNA));
        ConfiguratoreFixture fixture = fixture(true, view);

        fixture.controller.run();

        assertTrue(fixture.service.getCategorie().isEmpty());
    }

    @Test
    void run_conConfermeRimozioneNegate_nonRimuoveElementi() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CAMPI_COMUNI,
                IConfiguratoreView.MainAction.CATEGORIE,
                IConfiguratoreView.MainAction.LOGOUT);
        view.confermaRimozioneCampo = false;
        view.confermaRimozioneCategoria = false;
        view.commonFieldActions.addAll(List.of(
                ICampoConfigView.FieldAction.RIMUOVI,
                ICampoConfigView.FieldAction.TORNA));
        view.categoryActions.addAll(List.of(
                ICategoriaConfigView.CategoryAction.RIMUOVI,
                ICategoriaConfigView.CategoryAction.TORNA));
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.addCampoComune(new CampoDefinitionRequest("Eta", TipoDato.INTERO, false));
        fixture.service.createCategoria("Sport");

        fixture.controller.run();

        assertAll(
                () -> assertEquals(1, fixture.service.getCampiComuni().size()),
                () -> assertEquals(1, fixture.service.getCategorie().size())
        );
    }

    @Test
    void run_conErroreInAzioneCatalogo_mostraErrore() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CATEGORIE,
                IConfiguratoreView.MainAction.LOGOUT);
        view.categoryActions.addAll(List.of(
                ICategoriaConfigView.CategoryAction.CREA,
                ICategoriaConfigView.CategoryAction.TORNA));
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.createCategoria("Sport");

        fixture.controller.run();

        assertEquals(1, view.erroriMostrati);
    }

    @Test
    void run_creaCorreggePubblicaVisualizzaRitiraEArchiviaProposta() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CREA_PROPOSTA,
                IConfiguratoreView.MainAction.PUBBLICA_PROPOSTA,
                IConfiguratoreView.MainAction.BACHECA,
                IConfiguratoreView.MainAction.RITIRA_PROPOSTA,
                IConfiguratoreView.MainAction.ARCHIVIO,
                IConfiguratoreView.MainAction.LOGOUT);
        view.valoriProposta = java.util.Optional.of(Map.of(AppConstants.CAMPO_TITOLO, " "));
        view.correzioniProposta = java.util.Optional.of(valoriProposta("Torneo"));
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.createCategoria("Sport");

        fixture.controller.run();

        assertAll(
                () -> assertEquals(1, view.proposteSalvate),
                () -> assertEquals(1, view.propostePubblicate),
                () -> assertEquals(1, view.bachecheMostrate),
                () -> assertEquals(1, view.archiviMostrati),
                () -> assertEquals(1, view.esitiCatalogo)
        );
    }

    @Test
    void run_conCorrezionePropostaAnnullata_nonSalvaProposta() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CREA_PROPOSTA,
                IConfiguratoreView.MainAction.LOGOUT);
        view.valoriProposta = java.util.Optional.of(Map.of(AppConstants.CAMPO_TITOLO, " "));
        view.correzioniProposta = java.util.Optional.empty();
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.createCategoria("Sport");

        fixture.controller.run();

        assertEquals(0, view.proposteSalvate);
    }

    @Test
    void run_conCreazionePropostaAnnullata_mostraOperazioneAnnullata() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.CREA_PROPOSTA,
                IConfiguratoreView.MainAction.LOGOUT);
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.createCategoria("Sport");

        fixture.controller.run();

        assertEquals(1, view.operazioniAnnullate);
    }

    @Test
    void run_conPubblicazioneERitiroNonConfermati_nonEsegueOperazioni() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.PUBBLICA_PROPOSTA,
                IConfiguratoreView.MainAction.LOGOUT);
        view.confermaPubblicazione = false;
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.createCategoria("Sport");
        var proposta = fixture.service.creaProposta(fixture.service.getCategorie().get(0));
        fixture.service.applicaValoriEValida(proposta, valoriProposta("Conferme negate"));
        fixture.service.salvaProposta(proposta);

        fixture.controller.run();

        assertAll(
                () -> assertEquals(0, view.propostePubblicate),
                () -> assertEquals(0, view.esitiCatalogo)
        );
    }

    @Test
    void run_conRitiroNonConfermato_nonRitiraProposta() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.RITIRA_PROPOSTA,
                IConfiguratoreView.MainAction.LOGOUT);
        view.confermaRitiro = false;
        ConfiguratoreFixture fixture = fixture(true, view);
        fixture.service.createCategoria("Sport");
        var proposta = fixture.service.creaProposta(fixture.service.getCategorie().get(0));
        fixture.service.applicaValoriEValida(proposta, valoriProposta("Da non ritirare"));
        fixture.service.salvaProposta(proposta);
        fixture.service.pubblicaProposta(proposta);

        fixture.controller.run();

        assertEquals(0, view.esitiCatalogo);
    }

    @Test
    void run_conListeProposteVuote_nonMostraEsiti() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.PUBBLICA_PROPOSTA,
                IConfiguratoreView.MainAction.RITIRA_PROPOSTA,
                IConfiguratoreView.MainAction.IMPORTA,
                IConfiguratoreView.MainAction.LOGOUT);
        ConfiguratoreFixture fixture = fixture(true, view);

        fixture.controller.run();

        assertAll(
                () -> assertEquals(0, view.propostePubblicate),
                () -> assertEquals(0, view.esitiCatalogo),
                () -> assertEquals(0, view.erroriMostrati)
        );
    }

    @Test
    void run_conImportazioneDaFileAssente_mostraErrore() {
        MockConfiguratoreView view = new MockConfiguratoreView(
                IConfiguratoreView.MainAction.IMPORTA,
                IConfiguratoreView.MainAction.LOGOUT);
        view.percorsoImportazione = java.util.Optional.of(Path.of("file-import-inesistente.json"));
        ConfiguratoreFixture fixture = fixture(true, view);

        fixture.controller.run();

        assertEquals(1, view.erroriMostrati);
    }

    private ConfiguratoreFixture fixture(boolean configuraBase, MockConfiguratoreView view) {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        ConfiguratoreService service = new ConfiguratoreService(
                graph.catalogoService(),
                graph.propostaService(),
                new BatchImportService(graph.catalogoService(), graph.propostaService()));
        if (configuraBase) {
            service.configuraCampiBase(List.of());
        }
        return new ConfiguratoreFixture(view, service, new ConfiguratoreController(view, service));
    }

    private Map<String, String> valoriProposta(String titolo) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, "2",
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(5).format(AppConstants.DATE_FMT)
        );
    }

    private record ConfiguratoreFixture(
            MockConfiguratoreView view,
            ConfiguratoreService service,
            ConfiguratoreController controller) {
    }
}
