package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaServiceTest {
    @Test
    void creaValidaSalvaEPubblicaProposta_conCatalogoConfigurato_laRendeVisibileInBacheca() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        CatalogoService catalogoService = graph.catalogoService();
        PropostaService propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);

        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        PropostaValidationResult validationResult =
                propostaService.applicaValoriEValida(proposta, valoriProposta("Torneo di primavera", "4"));
        propostaService.salvaProposta(proposta);
        propostaService.pubblicaProposta(proposta);

        assertAll(
                () -> assertTrue(validationResult.valida()),
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertTrue(propostaService.getProposteValide().isEmpty()),
                () -> assertEquals(List.of(proposta), propostaService.getBachecaPerCategoria().get("Sport")),
                () -> assertEquals(1, graph.bachecaRepository().saveCount())
        );
    }

    @Test
    void costruttori_conDipendenzeNull_lancianoNullPointerException() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bachecaRepository =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaValidationService validationService = new PropostaValidationService();
        PropostaPublicationService publicationService = new PropostaPublicationService(bachecaRepository);
        PropostaLifecycleService lifecycleService = new PropostaLifecycleService(
                bachecaRepository,
                new it.unibs.ingsoft.application.notifica.NotificationService(
                        new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                it.unibs.ingsoft.domain.notifica.NotificaFactory.getInstance());
        PropostaQueryService queryService = new PropostaQueryService(bachecaRepository);

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new PropostaQueryService(null)),
                () -> assertThrows(NullPointerException.class, () -> new PropostaPublicationService(null)),
                () -> assertThrows(NullPointerException.class, () -> new PropostaLifecycleService(null,
                        new it.unibs.ingsoft.application.notifica.NotificationService(
                                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                        it.unibs.ingsoft.domain.notifica.NotificaFactory.getInstance())),
                () -> assertThrows(NullPointerException.class, () -> new PropostaLifecycleService(bachecaRepository, null,
                        it.unibs.ingsoft.domain.notifica.NotificaFactory.getInstance())),
                () -> assertThrows(NullPointerException.class, () -> new PropostaLifecycleService(bachecaRepository,
                        new it.unibs.ingsoft.application.notifica.NotificationService(
                                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                        null)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaService(null, publicationService, lifecycleService, queryService)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaService(validationService, null, lifecycleService, queryService)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaService(validationService, publicationService, null, queryService)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaService(validationService, publicationService, lifecycleService, null))
        );
    }

    @Test
    void salvaProposta_conNonValidaODuplicata_lanciaDomainFailureCorrente() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Categoria categoria = configuraCategoria(graph.catalogoService());
        Proposta nonValida = graph.propostaService().creaProposta(
                categoria,
                graph.catalogoService().getCampiBase(),
                graph.catalogoService().getCampiComuni());
        Proposta prima = propostaValida(graph, "Duplicata");
        Proposta seconda = propostaValidaNonSalvata(graph, "Duplicata");

        DomainException nonSalvabile = assertThrows(DomainException.class,
                () -> graph.propostaService().salvaProposta(nonValida));
        DomainException duplicata = assertThrows(DomainException.class,
                () -> graph.propostaService().salvaProposta(seconda));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotSavable.class, nonSalvabile.failure()),
                () -> assertInstanceOf(ProposalFailure.Duplicate.class, duplicata.failure())
        );
    }

    @Test
    void pubblicaProposta_conDuplicataInBacheca_lanciaDuplicate() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta esistente = propostaValida(graph, "Duplicata pubblicazione");
        Proposta duplicata = propostaValidaNonSalvata(graph, "Duplicata pubblicazione");
        graph.propostaService().pubblicaProposta(esistente);

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.propostaService().pubblicaProposta(duplicata));

        assertInstanceOf(ProposalFailure.Duplicate.class, exception.failure());
    }

    @Test
    void queryService_filtraIscrizioniRitirabiliStatiECategorie() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta apertaSport = propostaValida(graph, "Aperta sport");
        Proposta apertaMusica = propostaValida(graph, "Aperta musica", "Musica");
        Proposta confermata = propostaValida(graph, "Confermata sport");

        graph.propostaService().pubblicaProposta(apertaSport);
        graph.propostaService().pubblicaProposta(apertaMusica);
        graph.propostaService().pubblicaProposta(confermata);
        graph.propostaService().iscrivi(apertaSport, "anna");
        confermata.confermaSeAperta();

        assertAll(
                () -> assertEquals(List.of(apertaSport), graph.propostaService().getProposteAperteIscritteDa("anna")),
                () -> assertEquals(List.of(apertaSport, apertaMusica), graph.propostaService().getBacheca()),
                () -> assertEquals(List.of(apertaSport, apertaMusica, confermata),
                        graph.propostaService().getProposteRitirabili()),
                () -> assertEquals(List.of(apertaSport, apertaMusica, confermata),
                        graph.propostaService().getTutteLeProposte()),
                () -> assertEquals(List.of(apertaSport, apertaMusica),
                        graph.propostaService().getPropostePerStato().get(StatoProposta.APERTA)),
                () -> assertEquals(List.of(confermata),
                        graph.propostaService().getPropostePerStato().get(StatoProposta.CONFERMATA))
        );
    }

    @Test
    void lifecycleService_confermaRitiraENotificaAderenti() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta daConfermare = propostaValida(graph, "Da confermare");
        Proposta daRitirare = propostaValida(graph, "Da ritirare");

        graph.propostaService().pubblicaProposta(daConfermare);
        graph.propostaService().pubblicaProposta(daRitirare);
        graph.propostaService().iscrivi(daConfermare, "anna");
        graph.propostaService().iscrivi(daRitirare, "bruno");
        graph.propostaService().confermaProposta(daConfermare);
        graph.propostaService().ritiraProposta(daRitirare);

        assertAll(
                () -> assertTrue(daConfermare.isConfermata()),
                () -> assertEquals(StatoProposta.RITIRATA, daRitirare.getStato()),
                () -> assertEquals(2, graph.spazioPersonaleRepository().saveCount())
        );
    }

    @Test
    void lifecycleService_conNullONonPresente_lanciaNotFound() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta nonPersistita = propostaValidaNonSalvata(graph, "Non persistita");

        DomainException nullException = assertThrows(DomainException.class,
                () -> graph.propostaService().confermaProposta(null));
        DomainException nonPresenteException = assertThrows(DomainException.class,
                () -> graph.propostaService().ritiraProposta(nonPersistita));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotFound.class, nullException.failure()),
                () -> assertInstanceOf(ProposalFailure.NotFound.class, nonPresenteException.failure())
        );
    }

    private Categoria configuraCategoria(CatalogoService catalogoService) {
        catalogoService.configuraCampiBase(List.of());
        return catalogoService.createCategoria("Sport");
    }

    private Proposta propostaValida(ApplicationIntegrationSupport.ServiceGraph graph, String titolo) {
        return propostaValida(graph, titolo, "Sport");
    }

    private Proposta propostaValida(ApplicationIntegrationSupport.ServiceGraph graph, String titolo, String categoriaNome) {
        Proposta proposta = propostaValidaNonSalvata(graph, titolo, categoriaNome);
        graph.propostaService().salvaProposta(proposta);
        return proposta;
    }

    private Proposta propostaValidaNonSalvata(ApplicationIntegrationSupport.ServiceGraph graph, String titolo) {
        return propostaValidaNonSalvata(graph, titolo, "Sport");
    }

    private Proposta propostaValidaNonSalvata(ApplicationIntegrationSupport.ServiceGraph graph, String titolo, String categoriaNome) {
        CatalogoService catalogoService = graph.catalogoService();
        PropostaService propostaService = graph.propostaService();
        if (catalogoService.getCampiBase().isEmpty()) {
            catalogoService.configuraCampiBase(List.of());
        }
        Categoria categoria = catalogoService.getCategorie().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(categoriaNome))
                .findFirst()
                .orElseGet(() -> catalogoService.createCategoria(categoriaNome));
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        propostaService.applicaValoriEValida(proposta, valoriProposta(titolo, "4"));
        return proposta;
    }

    private Map<String, String> valoriProposta(String titolo, String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(5).format(AppConstants.DATE_FMT)
        );
    }
}
