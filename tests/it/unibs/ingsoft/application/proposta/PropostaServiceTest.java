package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.catalogo.Catalogo_Service;
import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.domain.TipoCampo;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.factory.NotificaFactory;
import it.unibs.ingsoft.domain.factory.PropostaFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaServiceTest {
    @Test
    void creaValidaSalvaEPubblicaProposta_conCatalogoConfigurato_laRendeVisibileInBacheca() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());

        PropostaValidationResult validationResult = propostaService.applicaValoriEValida(
                proposta,
                valoriProposta("Torneo di primavera", "4"));
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
    void salvaProposta_conChiaveIdentitaDuplicataInValide_lanciaIllegalStateException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();

        Categoria categoria = configuraCategoria(catalogoService);
        Proposta prima = propostaValida(propostaService, catalogoService, categoria, "Torneo duplicato");
        Proposta seconda = propostaValida(propostaService, catalogoService, categoria, "Torneo duplicato");

        propostaService.salvaProposta(prima);

        assertThrows(IllegalStateException.class, () -> propostaService.salvaProposta(seconda));
    }

    @Test
    void costruttori_conDipendenzeNull_lancianoNullPointerException() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bachecaRepository =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaQueryService queryService = new PropostaQueryService(bachecaRepository);
        PropostaPublication_Service publicationService =
                new PropostaPublication_Service(bachecaRepository, queryService);
        PropostaLifecycleService lifecycleService = new PropostaLifecycleService(
                bachecaRepository,
                new it.unibs.ingsoft.application.bacheca.NotificationService(
                        new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                NotificaFactory.getInstance());

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new PropostaCreationService(null)),
                () -> assertThrows(NullPointerException.class, () -> new PropostaQueryService(null)),
                () -> assertThrows(NullPointerException.class, () -> new PropostaPublication_Service(null)),
                () -> assertThrows(NullPointerException.class, () -> new PropostaPublication_Service(bachecaRepository, null)),
                () -> assertThrows(NullPointerException.class, () -> new PropostaLifecycleService(null,
                        new it.unibs.ingsoft.application.bacheca.NotificationService(
                                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                        NotificaFactory.getInstance())),
                () -> assertThrows(NullPointerException.class, () -> new PropostaLifecycleService(bachecaRepository, null,
                        NotificaFactory.getInstance())),
                () -> assertThrows(NullPointerException.class, () -> new PropostaLifecycleService(bachecaRepository,
                        new it.unibs.ingsoft.application.bacheca.NotificationService(
                                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                        null)),
                () -> assertThrows(NullPointerException.class, () -> new Proposta_Service(null,
                        new PropostaValidationService(), publicationService, lifecycleService, queryService)),
                () -> assertThrows(NullPointerException.class, () -> new Proposta_Service(
                        new PropostaCreationService(PropostaFactory.getInstance()),
                        null, publicationService, lifecycleService, queryService)),
                () -> assertThrows(NullPointerException.class, () -> new Proposta_Service(
                        new PropostaCreationService(PropostaFactory.getInstance()),
                        new PropostaValidationService(), null, lifecycleService, queryService)),
                () -> assertThrows(NullPointerException.class, () -> new Proposta_Service(
                        new PropostaCreationService(PropostaFactory.getInstance()),
                        new PropostaValidationService(), publicationService, null, queryService)),
                () -> assertThrows(NullPointerException.class, () -> new Proposta_Service(
                        new PropostaCreationService(PropostaFactory.getInstance()),
                        new PropostaValidationService(), publicationService, lifecycleService, null))
        );
    }

    @Test
    void publicationService_salvaNonValidaODuplicataInBacheca_lanciaDomainException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Categoria categoria = configuraCategoria(graph.catalogoService());
        Proposta nonValida = graph.propostaService().creaProposta(
                categoria,
                graph.catalogoService().getCampiBase(),
                graph.catalogoService().getCampiComuni());
        Proposta persistita = propostaValida(graph.propostaService(), graph.catalogoService(), categoria, "Duplicata bacheca");
        graph.propostaService().pubblicaProposta(persistita);
        Proposta duplicata = propostaValida(graph.propostaService(), graph.catalogoService(), categoria, "Duplicata bacheca");

        DomainException nonSalvabile = assertThrows(DomainException.class,
                () -> graph.propostaService().salvaProposta(nonValida));
        DomainException duplicataInBacheca = assertThrows(DomainException.class,
                () -> graph.propostaService().salvaProposta(duplicata));

        assertAll(
                () -> assertEquals(DomainErrorCode.PROPOSTA_NOT_SALVABILE, nonSalvabile.code()),
                () -> assertEquals(DomainErrorCode.PROPOSTA_DUPLICATA, duplicataInBacheca.code())
        );
    }

    @Test
    void publicationService_pubblicaDuplicataInBacheca_lanciaDomainExceptionESvuotaValide() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        Proposta esistente = propostaValida(propostaService, catalogoService, categoria, "Duplicata pubblicazione");
        propostaService.pubblicaProposta(esistente);
        Proposta duplicata = propostaValida(propostaService, catalogoService, categoria, "Duplicata pubblicazione");
        Proposta salvata = propostaValida(propostaService, catalogoService, categoria, "Solo valida");

        DomainException exception = assertThrows(DomainException.class, () -> propostaService.pubblicaProposta(duplicata));
        propostaService.salvaProposta(salvata);
        propostaService.clearProposteValide();

        assertAll(
                () -> assertEquals(DomainErrorCode.PROPOSTA_DUPLICATA, exception.code()),
                () -> assertTrue(propostaService.getProposteValide().isEmpty())
        );
    }

    @Test
    void publicationService_costruttoreCompatibileUsaRepositoryPerBacheca() throws Exception {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bachecaRepository =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaPublication_Service publicationService = new PropostaPublication_Service(bachecaRepository);
        Method bachecaMethod = PropostaPublication_Service.class.getDeclaredMethod("bacheca");
        bachecaMethod.setAccessible(true);

        Object bacheca = bachecaMethod.invoke(publicationService);

        assertSame(bachecaRepository.load(), bacheca);
    }

    @Test
    void queryService_filtraIscrizioniRitirabiliStatiECategorie() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria sport = configuraCategoria(catalogoService);
        Categoria musica = catalogoService.createCategoria("Musica");
        Proposta apertaSport = propostaValida(propostaService, catalogoService, sport, "Aperta sport");
        Proposta apertaMusica = propostaValida(propostaService, catalogoService, musica, "Aperta musica");
        Proposta confermata = propostaValida(propostaService, catalogoService, sport, "Confermata sport");

        propostaService.pubblicaProposta(apertaSport);
        propostaService.pubblicaProposta(apertaMusica);
        propostaService.pubblicaProposta(confermata);
        apertaSport.iscrivi("anna", LocalDate.now(AppConstants.clock));
        confermata.confermaSeAperta();

        Map<StatoProposta, List<Proposta>> perStato = propostaService.getPropostePerStato();
        Map<String, List<Proposta>> perCategoria = propostaService.getBachecaPerCategoria();

        assertAll(
                () -> assertEquals(List.of(), propostaService.getProposteAperteIscritteDa(null)),
                () -> assertEquals(List.of(apertaSport), propostaService.getProposteAperteIscritteDa("anna")),
                () -> assertEquals(List.of(apertaSport, apertaMusica), propostaService.getBacheca()),
                () -> assertEquals(List.of(apertaSport, apertaMusica, confermata), propostaService.getProposteRitirabili()),
                () -> assertEquals(List.of(apertaSport, apertaMusica, confermata), propostaService.getTutteLeProposte()),
                () -> assertEquals(List.of(apertaSport, apertaMusica), perStato.get(StatoProposta.APERTA)),
                () -> assertEquals(List.of(confermata), perStato.get(StatoProposta.CONFERMATA)),
                () -> assertEquals(List.of(apertaSport), perCategoria.get("Sport")),
                () -> assertEquals(List.of(apertaMusica), perCategoria.get("Musica"))
        );
    }

    @Test
    void lifecycleService_confermaRitiraENotificaAderenti() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        Proposta daConfermare = propostaValida(propostaService, catalogoService, categoria, "Da confermare");
        Proposta daRitirare = propostaValida(propostaService, catalogoService, categoria, "Da ritirare");

        propostaService.pubblicaProposta(daConfermare);
        propostaService.pubblicaProposta(daRitirare);
        daConfermare.iscrivi("anna", LocalDate.now(AppConstants.clock));
        daRitirare.iscrivi("bruno", LocalDate.now(AppConstants.clock));
        propostaService.confermaProposta(daConfermare);
        propostaService.ritiraProposta(daRitirare);

        assertAll(
                () -> assertTrue(daConfermare.isConfermata()),
                () -> assertEquals(StatoProposta.RITIRATA, daRitirare.getStato()),
                () -> assertEquals(2, graph.spazioPersonaleRepository().saveCount()),
                () -> assertEquals(4, graph.bachecaRepository().saveCount())
        );
    }

    @Test
    void lifecycleService_confermaNonApertaNonSalvaENullONonPresenteLancia() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        Proposta bozza = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        Proposta validaNonPersistita = propostaValida(propostaService, catalogoService, categoria, "Non persistita");

        graph.bachecaRepository().load().addProposta(bozza);
        propostaService.confermaProposta(bozza);
        DomainException nullException = assertThrows(DomainException.class, () -> propostaService.confermaProposta(null));
        DomainException nonPresenteException = assertThrows(DomainException.class,
                () -> propostaService.ritiraProposta(validaNonPersistita));

        assertAll(
                () -> assertEquals(0, graph.bachecaRepository().saveCount()),
                () -> assertEquals(DomainErrorCode.PROPOSTA_NON_TROVATA, nullException.code()),
                () -> assertEquals(DomainErrorCode.PROPOSTA_NON_TROVATA, nonPresenteException.code())
        );
    }

    @Test
    void lifecycleService_controllaScadenze_confermaAnnullaConcludeESalvaSoloSeCambia() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta daConfermare = propostaPersistitaConStato(catalogoService, categoria, "Scadenza confermata",
                oggi.minusDays(2), oggi.plusDays(2), oggi.plusDays(3), "1", StatoProposta.APERTA);
        Proposta daAnnullare = propostaPersistitaConStato(catalogoService, categoria, "Scadenza annullata",
                oggi.minusDays(2), oggi.plusDays(2), oggi.plusDays(3), "2", StatoProposta.APERTA);
        Proposta daConcludere = propostaPersistitaConStato(catalogoService, categoria, "Scadenza conclusa",
                oggi.minusDays(6), oggi.minusDays(4), oggi.minusDays(1), "1", StatoProposta.CONFERMATA);
        Proposta senzaCambio = propostaPersistitaConStato(catalogoService, categoria, "Scadenza futura",
                oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5), "1", StatoProposta.APERTA);

        graph.bachecaRepository().load().addProposta(daConfermare);
        graph.bachecaRepository().load().addProposta(daAnnullare);
        graph.bachecaRepository().load().addProposta(daConcludere);
        graph.bachecaRepository().load().addProposta(senzaCambio);
        daConfermare.iscrivi("anna", oggi.minusDays(3));

        propostaService.controllaScadenze();
        int saveDopoTransizioni = graph.bachecaRepository().saveCount();
        propostaService.controllaScadenze();

        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, daConfermare.getStato()),
                () -> assertEquals(StatoProposta.ANNULLATA, daAnnullare.getStato()),
                () -> assertEquals(StatoProposta.CONCLUSA, daConcludere.getStato()),
                () -> assertEquals(StatoProposta.APERTA, senzaCambio.getStato()),
                () -> assertEquals(1, saveDopoTransizioni),
                () -> assertEquals(saveDopoTransizioni, graph.bachecaRepository().saveCount()),
                () -> assertEquals(1, graph.spazioPersonaleRepository().saveCount())
        );
    }

    @Test
    void validationService_validaCampoCampiConErroreEApplicaValoriInvalidi() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                List.of(new Campo("Note", TipoCampo.COMUNE, TipoDato.STRINGA, true)));
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Map<String, String> valoriCorrenti = valoriProposta("Da validare", "2");
        List<ValidationError> erroriTermine = propostaService.validaCampo(
                proposta,
                valoriCorrenti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                oggi.minusDays(1).format(AppConstants.DATE_FMT));
        List<ValidationError> erroriData = propostaService.validaCampo(
                proposta,
                valoriCorrenti,
                AppConstants.CAMPO_DATA,
                oggi.plusDays(2).format(AppConstants.DATE_FMT));
        List<ValidationError> erroriConclusiva = propostaService.validaCampo(
                proposta,
                valoriCorrenti,
                AppConstants.CAMPO_DATA_CONCLUSIVA,
                oggi.plusDays(3).format(AppConstants.DATE_FMT));
        List<ValidationError> nessunErroreDefault = propostaService.validaCampo(
                proposta,
                valoriCorrenti,
                "Campo libero",
                "x");
        PropostaValidationResult invalidResult = propostaService.applicaValoriEValida(proposta, valoriCorrenti);

        assertAll(
                () -> assertFalse(erroriTermine.isEmpty()),
                () -> assertFalse(erroriData.isEmpty()),
                () -> assertFalse(erroriConclusiva.isEmpty()),
                () -> assertTrue(nessunErroreDefault.isEmpty()),
                () -> assertFalse(invalidResult.valida()),
                () -> assertEquals(List.of(campoPerNome(proposta, "Note")),
                        propostaService.getCampiConErrore(proposta, invalidResult.errori()))
        );
    }

    @Test
    void validationService_metodiDirettiDeleganoAllaProposta() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Catalogo_Service catalogoService = graph.catalogoService();
        Proposta_Service propostaService = graph.propostaService();
        Categoria categoria = configuraCategoria(catalogoService);
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());

        List<ValidationError> errori = propostaService.validaProposta(proposta);

        assertAll(
                () -> assertFalse(errori.isEmpty()),
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato())
        );
    }

    private Categoria configuraCategoria(Catalogo_Service catalogoService) {
        catalogoService.configuraCampiBase(List.of());
        return catalogoService.createCategoria("Sport");
    }

    private Proposta propostaValida(Proposta_Service propostaService,
                                    Catalogo_Service catalogoService,
                                    Categoria categoria,
                                    String titolo) {
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        propostaService.applicaValoriEValida(proposta, valoriProposta(titolo, "2"));
        return proposta;
    }

    private Map<String, String> valoriProposta(String titolo, String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return valoriPropostaConDate(
                titolo,
                numeroPartecipanti,
                oggi.plusDays(1),
                oggi.plusDays(4),
                oggi.plusDays(5));
    }

    private Proposta propostaConDate(Proposta_Service propostaService,
                                     Catalogo_Service catalogoService,
                                     Categoria categoria,
                                     String titolo,
                                     LocalDate termineIscrizione,
                                     LocalDate data,
                                     LocalDate dataConclusiva,
                                     String numeroPartecipanti) {
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        propostaService.applicaValoriEValida(
                proposta,
                valoriPropostaConDate(titolo, numeroPartecipanti, termineIscrizione, data, dataConclusiva));
        return proposta;
    }

    private Proposta propostaPersistitaConStato(Catalogo_Service catalogoService,
                                                Categoria categoria,
                                                String titolo,
                                                LocalDate termineIscrizione,
                                                LocalDate data,
                                                LocalDate dataConclusiva,
                                                String numeroPartecipanti,
                                                StatoProposta stato) {
        return Proposta.fromJson(
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni(),
                categoria,
                valoriPropostaConDate(titolo, numeroPartecipanti, termineIscrizione, data, dataConclusiva),
                stato,
                LocalDate.now(AppConstants.clock).minusDays(3),
                termineIscrizione,
                data,
                List.of(),
                null);
    }

    private Map<String, String> valoriPropostaConDate(String titolo,
                                                      String numeroPartecipanti,
                                                      LocalDate termineIscrizione,
                                                      LocalDate data,
                                                      LocalDate dataConclusiva) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termineIscrizione.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, data.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusiva.format(AppConstants.DATE_FMT)
        );
    }

    private Campo campoPerNome(Proposta proposta, String nomeCampo) {
        return proposta.getCampi().stream()
                .filter(campo -> campo.getNome().equals(nomeCampo))
                .findFirst()
                .orElseThrow();
    }
}
