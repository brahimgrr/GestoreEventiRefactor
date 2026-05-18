package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FruitoreControllerTest {
    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        MockFruitoreView view = new MockFruitoreView();

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new FruitoreController(null, graph.fruitoreService())),
                () -> assertThrows(NullPointerException.class, () -> new FruitoreController(view, null))
        );
    }

    @Test
    void run_conFruitoreNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();

        assertThrows(NullPointerException.class,
                () -> new FruitoreController(new MockFruitoreView(), graph.fruitoreService()).run(null));
    }

    @Test
    void run_iscriveEDisiscriveDaBacheca() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = pubblicaProposta(graph, "Torneo fruitore");
        Fruitore fruitore = new Fruitore("anna");
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.BACHECA,
                IFruitoreView.MainAction.DISDICI_ISCRIZIONE,
                IFruitoreView.MainAction.LOGOUT);

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertAll(
                () -> assertFalse(proposta.isIscritto(fruitore.getUsername())),
                () -> assertEquals(1, view.iscrizioniMostrate),
                () -> assertEquals(1, view.disiscrizioniMostrate)
        );
    }

    @Test
    void run_conIscrizioneNonConfermata_nonChiamaServizio() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = pubblicaProposta(graph, "Non confermata");
        Fruitore fruitore = new Fruitore("anna");
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.BACHECA,
                IFruitoreView.MainAction.LOGOUT);
        view.confermaIscrizione = false;

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertAll(
                () -> assertFalse(proposta.isIscritto(fruitore.getUsername())),
                () -> assertEquals(0, view.iscrizioniMostrate)
        );
    }

    @Test
    void run_conBachecaVuota_nonMostraIscrizione() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Fruitore fruitore = new Fruitore("anna");
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.BACHECA,
                IFruitoreView.MainAction.LOGOUT);

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertEquals(0, view.iscrizioniMostrate);
    }

    @Test
    void run_conErroreApplicativo_mostraErrore() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = pubblicaProposta(graph, "Capienza piena", "1");
        proposta.iscrivi("giaIscritto", LocalDate.now(AppConstants.clock));
        Fruitore fruitore = new Fruitore("anna");
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.BACHECA,
                IFruitoreView.MainAction.LOGOUT);

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertEquals(1, view.erroriMostrati);
    }

    @Test
    void run_conDisiscrizioneNonConfermata_nonDisiscrive() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = pubblicaProposta(graph, "Disdetta negata");
        Fruitore fruitore = new Fruitore("anna");
        graph.fruitoreService().iscrivi(proposta, fruitore);
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.DISDICI_ISCRIZIONE,
                IFruitoreView.MainAction.LOGOUT);
        view.confermaDisiscrizione = false;

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertAll(
                () -> assertTrue(proposta.isIscritto(fruitore.getUsername())),
                () -> assertEquals(0, view.disiscrizioniMostrate)
        );
    }

    @Test
    void run_senzaProposteDaDisdire_nonMostraDisiscrizione() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Fruitore fruitore = new Fruitore("anna");
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.DISDICI_ISCRIZIONE,
                IFruitoreView.MainAction.LOGOUT);

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertEquals(0, view.disiscrizioniMostrate);
    }

    @Test
    void run_gestisceSpazioPersonale_conEliminazioneEOscita() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Fruitore fruitore = new Fruitore("anna");
        Notifica notifica = new Notifica("Messaggio");
        new NotificationService(graph.spazioPersonaleRepository()).inviaNotifica(fruitore.getUsername(), notifica);
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.SPAZIO_PERSONALE,
                IFruitoreView.MainAction.LOGOUT);
        view.notificheSelezionate.add(Optional.of(notifica));
        view.notificheSelezionate.add(Optional.empty());

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertEquals(1, view.notificheEliminate);
    }

    @Test
    void run_gestisceSpazioPersonale_conConfermaNegata_nonElimina() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Fruitore fruitore = new Fruitore("anna");
        Notifica notifica = new Notifica("Messaggio");
        new NotificationService(graph.spazioPersonaleRepository()).inviaNotifica(fruitore.getUsername(), notifica);
        MockFruitoreView view = new MockFruitoreView(
                IFruitoreView.MainAction.SPAZIO_PERSONALE,
                IFruitoreView.MainAction.LOGOUT);
        view.confermaEliminazioneNotifica = false;
        view.notificheSelezionate.add(Optional.of(notifica));
        view.notificheSelezionate.add(Optional.empty());

        new FruitoreController(view, graph.fruitoreService()).run(fruitore);

        assertEquals(0, view.notificheEliminate);
    }

    private Proposta pubblicaProposta(ApplicationIntegrationSupport.ServiceGraph graph, String titolo) {
        return pubblicaProposta(graph, titolo, "2");
    }

    private Proposta pubblicaProposta(ApplicationIntegrationSupport.ServiceGraph graph, String titolo, String numeroPartecipanti) {
        graph.catalogoService().configuraCampiBase(List.of());
        var categoria = graph.catalogoService().createCategoria("Sport");
        Proposta proposta = graph.propostaService().creaProposta(
                categoria,
                graph.catalogoService().getCampiBase(),
                graph.catalogoService().getCampiComuni());
        graph.propostaService().applicaValoriEValida(proposta, valoriProposta(titolo, numeroPartecipanti));
        graph.propostaService().salvaProposta(proposta);
        graph.propostaService().pubblicaProposta(proposta);
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
