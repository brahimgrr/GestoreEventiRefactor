package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.ArchivioNotifiche;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.NotificaType;
import it.unibs.ingsoft.domain.SpazioPersonale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileSpazioPersonaleRepository_Test {
    @TempDir
    Path tempDir;

    @Test
    void load_conFileAssente_restituisceArchivioNotificheVuoto() {
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));

        ArchivioNotifiche archivio = repository.load();

        assertTrue(archivio.getUtenti().isEmpty());
    }

    /*
    IN TUTTI GLI ALTRI (TRANNE FACTORY) l'istanza restituita non è la stessa
     */
    @Test
    void getSpazioDi_conStessoUsernameDueVolte_restituisceStessaIstanzaNelloStessoArchivio() {
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));
        ArchivioNotifiche archivio = repository.load();

        assertSame(archivio.getSpazioDi("mario"), archivio.getSpazioDi("mario"));
    }

    @Test
    void getSpazioDi_conUsernameDiversi_restituisceSpaziPersonaliDiversi() {
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));
        ArchivioNotifiche archivio = repository.load();

        assertNotSame(archivio.getSpazioDi("mario"), archivio.getSpazioDi("luigi"));
    }

    @Test
    void save_conArchivioVuoto_creaFile() {
        Path path = tempDir.resolve("notifiche.json");

        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(path);
        repository.save(new ArchivioNotifiche());

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conArchivioModificato_persisteLaNotificaDellUtente() {
        Path path = tempDir.resolve("notifiche.json");

        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(path);
        ArchivioNotifiche archivio = repository.load();
        archivio.getSpazioDi("mario").addNotifica(
                new Notifica("id-1", NotificaType.LEGACY_MESSAGGIO, Map.of(), "messaggio", LocalDateTime.of(2026, 5, 6, 10, 0)));
        repository.save(archivio);

        SpazioPersonale ricaricato = new FileSpazioPersonaleRepository(path).load().getSpazioDi("mario");

        assertEquals("id-1", ricaricato.getNotifiche().get(0).id());
    }
}
