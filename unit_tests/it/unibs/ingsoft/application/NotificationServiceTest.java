package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.testsupport.InMemorySpazioPersonaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    private InMemorySpazioPersonaleRepository repo;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repo = new InMemorySpazioPersonaleRepository();
        service = new NotificationService(repo);
    }

    @Test
    void UC17_visualizzareSpazioPersonale_returnsUserNotifications() {
        Notifica notifica = new Notifica("Messaggio");

        service.inviaNotifica("mario", notifica);
        List<Notifica> result = service.getNotifiche("mario");

        assertEquals(List.of(notifica), result);
        assertEquals(1, repo.saveCount());
    }

    @Test
    void UC18_eliminareNotifica_success_removesAndPersists() {
        Notifica notifica = new Notifica("Messaggio");
        service.inviaNotifica("mario", notifica);

        service.cancellaNotifica("mario", notifica);

        assertTrue(service.getNotifiche("mario").isEmpty());
        assertEquals(2, repo.saveCount());
    }

    @Test
    void UC17_inviaNotifica_nullInputsAreIgnored() {
        service.inviaNotifica(null, new Notifica("Messaggio"));
        service.inviaNotifica("mario", null);

        assertEquals(0, repo.saveCount());
        assertTrue(service.getNotifiche("mario").isEmpty());
    }
}
