package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.utente.Credenziali;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileCredenzialiRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void get_conFileAssente_restituisceCredenzialiVuote() {
        FileCredenzialiRepository repository = new FileCredenzialiRepository(tempDir.resolve("utenti.json"));

        Credenziali credenziali = repository.get();

        assertTrue(credenziali.getConfiguratori().isEmpty());
    }

    @Test
    void get_quandoInvocatoDueVolte_restituisceStessaIstanzaCached() {
        FileCredenzialiRepository repository = new FileCredenzialiRepository(tempDir.resolve("utenti.json"));

        assertSame(repository.get(), repository.get());
    }

    @Test
    void save_senzaGetPrecedente_nonCreaFile() {
        Path path = tempDir.resolve("utenti.json");
        FileCredenzialiRepository repository = new FileCredenzialiRepository(path);

        repository.save();

        assertFalse(Files.exists(path));
    }

    @Test
    void save_dopoModificaDelleCredenzialiCached_persisteFruitore() {
        Path path = tempDir.resolve("utenti.json");
        FileCredenzialiRepository repository = new FileCredenzialiRepository(path);
        repository.get().addFruitore("Mario", "pwd");

        repository.save();
        Credenziali ricaricate = new FileCredenzialiRepository(path).get();

        assertEquals("pwd", ricaricate.getFruitori().get("mario"));
    }
}
