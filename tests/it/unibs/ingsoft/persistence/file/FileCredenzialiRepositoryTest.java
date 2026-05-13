package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileCredenzialiRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void load_conFileAssente_restituisceCredenzialiVuote() {
        FileCredenzialiRepository repository = new FileCredenzialiRepository(tempDir.resolve("utenti.json"));

        CredenzialiDTO credenziali = repository.load();

        assertTrue(credenziali.getConfiguratori().isEmpty());
    }

    @Test
    void load_quandoInvocatoDueVolte_restituisceIstanzeDistinte() {
        FileCredenzialiRepository repository = new FileCredenzialiRepository(tempDir.resolve("utenti.json"));

        assertNotSame(repository.load(), repository.load());
    }

    @Test
    void save_conCredenzialiVuote_creaFile() {
        Path path = tempDir.resolve("utenti.json");

        FileCredenzialiRepository repository = new FileCredenzialiRepository(path);
        repository.save(new CredenzialiDTO());

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conCredenzialiModificate_persisteFruitore() {
        Path path = tempDir.resolve("utenti.json");

        FileCredenzialiRepository repository = new FileCredenzialiRepository(path);
        CredenzialiDTO credenziali = repository.load();
        credenziali.addFruitore("Mario", "pwd");
        repository.save(credenziali);

        CredenzialiDTO ricaricate = new FileCredenzialiRepository(path).load();

        assertEquals("pwd", ricaricate.getFruitori().get("mario"));
    }
}
