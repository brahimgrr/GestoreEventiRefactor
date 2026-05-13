package it.unibs.ingsoft.application.notifica;

import it.unibs.ingsoft.domain.notifica.ArchivioNotifiche;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.utente.SpazioPersonale;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    @Test
    void getNotificheDoesNotCreateMissingUserSpace() {
        InMemorySpazioPersonaleRepository repository = new InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);

        List<Notifica> notifiche = service.getNotifiche("alice");

        assertTrue(notifiche.isEmpty());
        assertFalse(repository.load().getUtenti().containsKey("alice"));
        assertEquals(0, repository.saveCount);
    }

    @Test
    void inviaNotificaCreatesAndPersistsUserSpace() {
        InMemorySpazioPersonaleRepository repository = new InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);
        Notifica notifica = new Notifica("Evento confermato");

        service.inviaNotifica("alice", notifica);

        SpazioPersonale spazio = repository.load().findSpazioDi("alice").orElseThrow();
        assertEquals(List.of(notifica), spazio.getNotifiche());
        assertEquals(1, repository.saveCount);
    }

    @Test
    void cancellaNotificaDoesNotCreateOrPersistWhenUserIsMissing() {
        InMemorySpazioPersonaleRepository repository = new InMemorySpazioPersonaleRepository();
        NotificationService service = new NotificationService(repository);

        service.cancellaNotifica("alice", new Notifica("Mai inviata"));

        assertFalse(repository.load().getUtenti().containsKey("alice"));
        assertEquals(0, repository.saveCount);
    }

    private static final class InMemorySpazioPersonaleRepository implements ISpazioPersonaleRepository {
        private ArchivioNotifiche archivio = new ArchivioNotifiche();
        private int saveCount;

        @Override
        public ArchivioNotifiche load() {
            return archivio;
        }

        @Override
        public void save(ArchivioNotifiche archivio) {
            this.archivio = archivio;
            saveCount++;
        }
    }
}
