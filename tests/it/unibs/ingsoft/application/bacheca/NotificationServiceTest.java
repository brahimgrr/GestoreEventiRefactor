package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.domain.model.notifica.Notifica;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegacyNotificationServiceTest {
    @Test
    void costruttore_conRepositoryNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NotificationService(null));
    }

    @Test
    void inviaNotifica_conUsernameENotificaValidi_persisteNotificaNelloSpazioPersonale() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);
        Notifica notifica = new Notifica("messaggio");

        service.inviaNotifica("mario", notifica);

        assertAll(
                () -> assertEquals(1, service.getNotifiche("mario").size()),
                () -> assertEquals(notifica, service.getNotifiche("mario").get(0)),
                () -> assertEquals(1, repository.saveCount())
        );
    }

    @Test
    void cancellaNotifica_conNotificaPresente_laRimuoveEPersisteArchivio() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();

        NotificationService service = new NotificationService(repository);

        Notifica notifica = new Notifica("messaggio");
        service.inviaNotifica("mario", notifica);
        service.cancellaNotifica("mario", notifica);

        assertAll(
                () -> assertTrue(service.getNotifiche("mario").isEmpty()),
                () -> assertEquals(2, repository.saveCount())
        );
    }

    @Test
    void inviaNotifica_conUsernameNull_nonPersisteNulla() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);

        service.inviaNotifica(null, new Notifica("messaggio"));

        assertEquals(0, repository.saveCount());
    }

    @Test
    void inviaNotifica_conNotificaNull_nonPersisteNulla() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);

        service.inviaNotifica("mario", null);

        assertEquals(0, repository.saveCount());
    }

    @Test
    void getNotifiche_conUsernameNullOUtenteSenzaSpazio_restituisceListaVuota() {
        NotificationService service = new NotificationService(
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository());

        assertAll(
                () -> assertTrue(service.getNotifiche(null).isEmpty()),
                () -> assertTrue(service.getNotifiche("assente").isEmpty())
        );
    }

    @Test
    void cancellaNotifica_conUsernameONotificaNull_nonPersisteNulla() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);

        service.cancellaNotifica(null, new Notifica("messaggio"));
        service.cancellaNotifica("mario", null);

        assertEquals(0, repository.saveCount());
    }

    @Test
    void cancellaNotifica_conSpazioAssente_nonPersisteNulla() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);

        service.cancellaNotifica("mario", new Notifica("messaggio"));

        assertEquals(0, repository.saveCount());
    }

    @Test
    void cancellaNotifica_conNotificaAssente_nonPersisteArchivio() {
        ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository repository =
                new ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);
        service.inviaNotifica("mario", new Notifica("presente"));

        service.cancellaNotifica("mario", new Notifica("assente"));

        assertAll(
                () -> assertEquals(1, service.getNotifiche("mario").size()),
                () -> assertEquals(1, repository.saveCount())
        );
    }
}
