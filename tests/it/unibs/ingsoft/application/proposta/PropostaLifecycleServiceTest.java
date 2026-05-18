package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
import it.unibs.ingsoft.domain.proposta.EsitoTransizioneProposta;
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

class PropostaLifecycleServiceTest {
    @Test
    void costruttoreCompleto_conDipendenzeNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        NotificationService notificationService = new NotificationService(
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository());
        NotificaFactory factory = NotificaFactory.getInstance();
        PropostaCommandLock lock = new PropostaCommandLock();

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaLifecycleService(null, notificationService, factory, lock)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaLifecycleService(bacheca, null, factory, lock)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaLifecycleService(bacheca, notificationService, null, lock)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaLifecycleService(bacheca, notificationService, factory, null))
        );
    }

    @Test
    void controllaScadenze_quandoNessunaTransizione_nonSalva() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        Proposta apertaNonScaduta = proposta(
                "Futura",
                StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of());
        bacheca.load().addProposta(apertaNonScaduta);

        service(bacheca).controllaScadenze();

        assertEquals(0, bacheca.saveCount());
    }

    @Test
    void controllaScadenze_conTransizioni_salvaENotificaQuandoServe() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository spazi =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta daConfermare = proposta("Confermare", StatoProposta.APERTA, oggi.minusDays(1), oggi.plusDays(2), List.of("anna", "bruno"));
        Proposta daAnnullare = proposta("Annullare", StatoProposta.APERTA, oggi.minusDays(1), oggi.plusDays(2), List.of("carlo"));
        Proposta daConcludere = proposta("Concludere", StatoProposta.CONFERMATA, oggi.minusDays(5), oggi.minusDays(2), List.of("dora"));
        bacheca.load().addProposta(daConfermare);
        bacheca.load().addProposta(daAnnullare);
        bacheca.load().addProposta(daConcludere);

        new PropostaLifecycleService(
                bacheca,
                new NotificationService(spazi),
                NotificaFactory.getInstance()).controllaScadenze();

        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, daConfermare.getStato()),
                () -> assertEquals(StatoProposta.ANNULLATA, daAnnullare.getStato()),
                () -> assertEquals(StatoProposta.CONCLUSA, daConcludere.getStato()),
                () -> assertEquals(1, bacheca.saveCount()),
                () -> assertEquals(3, spazi.saveCount())
        );
    }

    @Test
    void confermaProposta_conPropostaNonAperta_nonSalva() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        Proposta confermata = proposta("Gia confermata", StatoProposta.CONFERMATA,
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of("anna"));
        bacheca.load().addProposta(confermata);

        service(bacheca).confermaProposta(confermata);

        assertEquals(0, bacheca.saveCount());
    }

    @Test
    void confermaProposta_conPropostaAperta_salvaENotificaAderenti() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository spazi =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        Proposta aperta = proposta("Da confermare", StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of("anna", "bruno"));
        bacheca.load().addProposta(aperta);

        new PropostaLifecycleService(
                bacheca,
                new NotificationService(spazi),
                NotificaFactory.getInstance()).confermaProposta(aperta);

        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, aperta.getStato()),
                () -> assertEquals(1, bacheca.saveCount()),
                () -> assertEquals(2, spazi.saveCount())
        );
    }

    @Test
    void iscriviDisiscriviRitira_operanoSuPropostaPersistita() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        Proposta proposta = proposta("Aperta", StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of());
        bacheca.load().addProposta(proposta);
        PropostaLifecycleService service = service(bacheca);

        service.iscrivi(proposta, "mario");
        service.disiscrivi(proposta, "mario");
        service.ritiraProposta(proposta);

        assertAll(
                () -> assertFalse(proposta.isIscritto("mario")),
                () -> assertEquals(StatoProposta.RITIRATA, proposta.getStato()),
                () -> assertEquals(3, bacheca.saveCount())
        );
    }

    @Test
    void iscrivi_conUltimoPostoDisponibile_confermaENotifica() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository spazi =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        Proposta proposta = proposta("Posto unico", StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4),
                "1",
                List.of());
        bacheca.load().addProposta(proposta);

        new PropostaLifecycleService(
                bacheca,
                new NotificationService(spazi),
                NotificaFactory.getInstance()).iscrivi(proposta, "mario");

        assertAll(
                () -> assertTrue(proposta.isIscritto("mario")),
                () -> assertEquals(StatoProposta.CONFERMATA, proposta.getStato()),
                () -> assertEquals(1, bacheca.saveCount()),
                () -> assertEquals(1, spazi.saveCount())
        );
    }

    @Test
    void metodi_conPropostaNonPersistita_lancianoNotFound() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        Proposta assente = proposta("Assente", StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of());
        PropostaLifecycleService service = service(bacheca);

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotFound.class,
                        assertThrows(DomainException.class, () -> service.confermaProposta(assente)).failure()),
                () -> assertInstanceOf(ProposalFailure.NotFound.class,
                        assertThrows(DomainException.class, () -> service.iscrivi(assente, "mario")).failure()),
                () -> assertInstanceOf(ProposalFailure.NotFound.class,
                        assertThrows(DomainException.class, () -> service.disiscrivi(assente, "mario")).failure()),
                () -> assertInstanceOf(ProposalFailure.NotFound.class,
                        assertThrows(DomainException.class, () -> service.ritiraProposta(assente)).failure())
        );
    }

    private PropostaLifecycleService service(ApplicationIntegrationSupport.InMemoryBachecaRepository bacheca) {
        return new PropostaLifecycleService(
                bacheca,
                new NotificationService(new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository()),
                NotificaFactory.getInstance());
    }

    private Proposta proposta(String titolo,
                              StatoProposta stato,
                              LocalDate termine,
                              LocalDate dataEvento,
                              List<String> aderenti) {
        return proposta(titolo, stato, termine, dataEvento, "2", aderenti);
    }

    private Proposta proposta(String titolo,
                              StatoProposta stato,
                              LocalDate termine,
                              LocalDate dataEvento,
                              String numeroPartecipanti,
                              List<String> aderenti) {
        return Proposta.fromJson(
                titolo.toLowerCase(),
                List.of(),
                List.of(),
                new Categoria("Sport"),
                Map.of(
                        AppConstants.CAMPO_TITOLO, titolo,
                        AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                        AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_DATA_CONCLUSIVA, dataEvento.format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_ORA, "16:30",
                        AppConstants.CAMPO_LUOGO, "Brescia"
                ),
                stato,
                LocalDate.now(AppConstants.clock).minusDays(2),
                termine,
                dataEvento,
                aderenti,
                null);
    }
}
