package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBachecaRepositoryTest {
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of("out"));
        tempDir = Files.createTempDirectory(Path.of("out"), "file-bacheca-repository-test-");
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
    void load_conFileAssente_restituisceBachecaVuota() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        BachecaDTO bacheca = repository.load();

        assertTrue(bacheca.getProposte().isEmpty());
    }

    @Test
    void load_quandoInvocatoDueVolte_restituisceIstanzeDistinte() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        assertNotSame(repository.load(), repository.load());
    }

    @Test
    void save_conBachecaVuota_creaFile() throws Exception {
        Path path = tempDir.resolve("bacheca.json");

        FileBachecaRepository repository = new FileBachecaRepository(path);
        repository.save(new BachecaDTO());

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conBachecaModificata_persisteLaProposta() {
        Path path = tempDir.resolve("bacheca.json");

        FileBachecaRepository repository = new FileBachecaRepository(path);
        BachecaDTO bacheca = repository.load();
        bacheca.addProposta(new Proposta(new Categoria("Sport"), List.of(), List.of()));
        repository.save(bacheca);

        BachecaDTO ricaricata = new FileBachecaRepository(path).load();

        assertEquals(1, ricaricata.getProposte().size());
    }

    @Test
    void costruttore_conPathNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FileBachecaRepository(null));
    }

    @Test
    void save_conBachecaNull_lanciaNullPointerException() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        assertThrows(NullPointerException.class, () -> repository.save(null));
    }
}
