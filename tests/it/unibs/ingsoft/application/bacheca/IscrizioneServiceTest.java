package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.FruitoreService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.model.notifica.NotificaType;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IscrizioneServiceTest {
    @Test
    void iscrivi_conPropostaPersistitaAperta_aggiungeFruitoreESalvaBacheca() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Proposta proposta = propostaPubblicata(graph, "Evento aperto", "2");

        fruitoreService.iscrivi(proposta, new Fruitore("mario"));

        assertAll(
                () -> assertTrue(proposta.isIscritto("mario")),
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(2, graph.bachecaRepository().saveCount())
        );
    }

    @Test
    void iscrivi_conUltimoPostoDisponibile_confermaPropostaEInviaNotifica() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Proposta proposta = propostaPubblicata(graph, "Evento a posto unico", "1");

        fruitoreService.iscrivi(proposta, new Fruitore("mario"));

        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, proposta.getStato()),
                () -> assertEquals(1, fruitoreService.getNotifiche(new Fruitore("mario")).size()),
                () -> assertEquals(NotificaType.PROPOSTA_CONFERMATA,
                        fruitoreService.getNotifiche(new Fruitore("mario")).get(0).type())
        );
    }

    @Test
    void disiscrivi_conPropostaPersistitaAperta_rimuoveFruitoreESalvaBacheca() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        FruitoreService fruitoreService = graph.fruitoreService();
        Proposta proposta = propostaPubblicata(graph, "Evento con disiscrizione", "2");
        Fruitore mario = new Fruitore("mario");
        fruitoreService.iscrivi(proposta, mario);

        fruitoreService.disiscrivi(proposta, mario);

        assertAll(
                () -> assertFalse(proposta.isIscritto("mario")),
                () -> assertEquals(3, graph.bachecaRepository().saveCount())
        );
    }

    @Test
    void iscriviODisiscrivi_conPropostaNonPersistita_lanciaNotFoundENonSalva() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = propostaApertaNonPersistita(graph, "Evento assente", "2");

        DomainException iscrizione = assertThrows(DomainException.class,
                () -> graph.fruitoreService().iscrivi(proposta, new Fruitore("mario")));
        DomainException disiscrizione = assertThrows(DomainException.class,
                () -> graph.fruitoreService().disiscrivi(proposta, new Fruitore("mario")));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotFound.class, iscrizione.failure()),
                () -> assertInstanceOf(ProposalFailure.NotFound.class, disiscrizione.failure()),
                () -> assertEquals(0, graph.bachecaRepository().saveCount())
        );
    }

    @Test
    void iscriviODisiscrivi_conFruitoreNull_lanciaNullPointerExceptionENonSalva() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        Proposta proposta = propostaPubblicata(graph, "Evento fruitore null", "2");

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> graph.fruitoreService().iscrivi(proposta, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> graph.fruitoreService().disiscrivi(proposta, null)),
                () -> assertEquals(1, graph.bachecaRepository().saveCount())
        );
    }

    private Proposta propostaPubblicata(ApplicationIntegrationSupport.ServiceGraph graph,
                                        String titolo,
                                        String numeroPartecipanti) {
        Proposta proposta = propostaValida(graph, titolo, numeroPartecipanti);
        graph.propostaService().pubblicaProposta(proposta);
        return proposta;
    }

    private Proposta propostaApertaNonPersistita(ApplicationIntegrationSupport.ServiceGraph graph,
                                                 String titolo,
                                                 String numeroPartecipanti) {
        Proposta proposta = propostaValida(graph, titolo, numeroPartecipanti);
        proposta.pubblica(LocalDate.now(AppConstants.clock));
        return proposta;
    }

    private Proposta propostaValida(ApplicationIntegrationSupport.ServiceGraph graph,
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
