package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaType;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FruitoreServiceTest {
    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        it.unibs.ingsoft.application.notifica.NotificationService notificationService =
                new it.unibs.ingsoft.application.notifica.NotificationService(graph.spazioPersonaleRepository());

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new FruitoreService(null, notificationService)),
                () -> assertThrows(NullPointerException.class,
                        () -> new FruitoreService(graph.propostaService(), null))
        );
    }

    @Test
    void getBachecaPerCategoria_delegaAlServizioProposte() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Proposta proposta = propostaPubblicata(graph, "Corso in bacheca", "2");

        assertEquals(List.of(proposta), fruitoreService.getBachecaPerCategoria().get("Sport"));
    }

    @Test
    void iscriviEDisiscrivi_conPropostaAperta_aggiornaProposteAperteIscritte() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Fruitore mario = new Fruitore("mario");
        Proposta proposta = propostaPubblicata(graph, "Corso aperto", "2");

        fruitoreService.iscrivi(proposta, mario);

        assertEquals(List.of(proposta), fruitoreService.getProposteAperteIscritteDa(mario));

        fruitoreService.disiscrivi(proposta, mario);

        assertTrue(fruitoreService.getProposteAperteIscritteDa(mario).isEmpty());
    }

    @Test
    void iscrivi_conUltimoPostoDisponibile_confermaPropostaEInviaNotifica() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Fruitore mario = new Fruitore("mario");
        Proposta proposta = propostaPubblicata(graph, "Corso a posto unico", "1");

        fruitoreService.iscrivi(proposta, mario);

        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, proposta.getStato()),
                () -> assertEquals(1, fruitoreService.getNotifiche(mario).size()),
                () -> assertEquals(NotificaType.PROPOSTA_CONFERMATA,
                        fruitoreService.getNotifiche(mario).get(0).type()),
                () -> assertEquals(1, graph.spazioPersonaleRepository().saveCount())
        );
    }

    @Test
    void iscrivi_conNessunPostoDisponibile_confermaPropostaEInviaNotifica() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();

        Fruitore mario = new Fruitore("mario");
        Proposta proposta = propostaPubblicata(graph, "Corso a posto unico", "1");
        fruitoreService.iscrivi(proposta, mario);

        Fruitore luigi = new Fruitore("luigi");

        assertThrows(DomainException.class, () ->fruitoreService.iscrivi(proposta, luigi));
    }

    @Test
    void iscriviODisiscrivi_conPropostaNonPersistita_lanciaNotFoundENonSalva() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = propostaApertaNonPersistita(graph, "Evento assente", "2");
        Fruitore mario = new Fruitore("mario");

        DomainException iscrizione = assertThrows(DomainException.class,
                () -> graph.fruitoreService().iscrivi(proposta, mario));
        DomainException disiscrizione = assertThrows(DomainException.class,
                () -> graph.fruitoreService().disiscrivi(proposta, mario));

        assertAll(
                () -> assertInstanceOf(it.unibs.ingsoft.domain.proposta.ProposalFailure.NotFound.class,
                        iscrizione.failure()),
                () -> assertInstanceOf(it.unibs.ingsoft.domain.proposta.ProposalFailure.NotFound.class,
                        disiscrizione.failure()),
                () -> assertEquals(0, graph.bachecaRepository().saveCount())
        );
    }

    @Test
    void notifiche_possonoEssereLettereECancellateDalFruitore() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Fruitore mario = new Fruitore("mario");
        Proposta proposta = propostaPubblicata(graph, "Corso con notifica", "1");

        assertTrue(fruitoreService.getNotifiche(mario).isEmpty());
        fruitoreService.iscrivi(proposta, mario);
        Notifica notifica = fruitoreService.getNotifiche(mario).get(0);
        fruitoreService.cancellaNotifica(mario, notifica);

        assertTrue(fruitoreService.getNotifiche(mario).isEmpty());
    }

    @Test
    void metodiConFruitoreNull_lancianoNullPointerExceptionPrimaDellaDelega() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Proposta proposta = propostaPubblicata(graph, "Corso null fruitore", "2");
        Notifica notifica = new Notifica("Promemoria");

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> fruitoreService.getProposteAperteIscritteDa(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> fruitoreService.iscrivi(proposta, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> fruitoreService.disiscrivi(proposta, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> fruitoreService.getNotifiche(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> fruitoreService.cancellaNotifica(null, notifica))
        );
    }

    private Proposta propostaPubblicata(ApplicationIntegrationSupport.ServiceGraph graph,
                                        String titolo,
                                        String numeroPartecipanti) {
        CatalogoService catalogoService = graph.catalogoService();
        PropostaService propostaService = graph.propostaService();
        catalogoService.configuraCampiBase(List.of());
        Categoria categoria = catalogoService.createCategoria("Sport");
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        propostaService.applicaValoriEValida(proposta, valoriProposta(titolo, numeroPartecipanti));
        propostaService.salvaProposta(proposta);
        propostaService.pubblicaProposta(proposta);
        return proposta;
    }

    private Proposta propostaApertaNonPersistita(ApplicationIntegrationSupport.ServiceGraph graph,
                                                 String titolo,
                                                 String numeroPartecipanti) {
        Proposta proposta = propostaNonPubblicataValida(graph, titolo, numeroPartecipanti);
        proposta.pubblica(LocalDate.now(AppConstants.clock));
        return proposta;
    }

    private Proposta propostaNonPubblicataValida(ApplicationIntegrationSupport.ServiceGraph graph,
                                                 String titolo,
                                                 String numeroPartecipanti) {
        CatalogoService catalogoService = graph.catalogoService();
        PropostaService propostaService = graph.propostaService();
        catalogoService.configuraCampiBase(List.of());
        Categoria categoria = catalogoService.createCategoria("Sport");
        Proposta proposta = propostaService.creaProposta(
                categoria,
                catalogoService.getCampiBase(),
                catalogoService.getCampiComuni());
        propostaService.applicaValoriEValida(proposta, valoriProposta(titolo, numeroPartecipanti));
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
