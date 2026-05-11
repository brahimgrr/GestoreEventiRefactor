package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.utente.SpazioPersonale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileSpazioPersonaleRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void get_conFileAssente_restituisceSpazioPersonaleVuoto() {
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));

        SpazioPersonale spazio = repository.get("mario");

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void get_conStessoUsernameDueVolte_restituisceStessaIstanzaCached() {
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));

        assertSame(repository.get("mario"), repository.get("mario"));
    }

    @Test
    void get_conUsernameDiversi_restituisceSpaziPersonaliDiversi() {
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));

        assertNotSame(repository.get("mario"), repository.get("luigi"));
    }

    @Test
    void save_senzaGetPrecedente_nonCreaFile() {
        Path path = tempDir.resolve("notifiche.json");
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(path);

        repository.save();

        assertFalse(Files.exists(path));
    }

    @Test
    void save_dopoModificaDelloSpazioCached_persistelaNotificaDellUtente() {
        Path path = tempDir.resolve("notifiche.json");
        FileSpazioPersonaleRepository repository = new FileSpazioPersonaleRepository(path);
        repository.get("mario").addNotifica(new Notifica("id-1", "messaggio", LocalDateTime.of(2026, 5, 6, 10, 0)));

        repository.save();
        SpazioPersonale ricaricato = new FileSpazioPersonaleRepository(path).get("mario");

        assertEquals("id-1", ricaricato.getNotifiche().get(0).id());
    }
}
