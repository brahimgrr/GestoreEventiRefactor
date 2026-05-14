package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FileCredenzialiRepositoryTest {
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of("out"));
        tempDir = Files.createTempDirectory(Path.of("out"), "file-credenziali-repository-test-");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (var paths = Files.walk(tempDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // Best effort cleanup for test artifacts.
                            }
                        });
            }
        }
    }

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

    @Test
    void costruttore_conPathNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FileCredenzialiRepository(null));
    }

    @Test
    void save_conCredenzialiNull_lanciaNullPointerException() {
        FileCredenzialiRepository repository = new FileCredenzialiRepository(tempDir.resolve("utenti.json"));

        assertThrows(NullPointerException.class, () -> repository.save(null));
    }
}
