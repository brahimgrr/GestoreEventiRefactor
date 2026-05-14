package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.Catalogo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FileCatalogoRepositoryTest {
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of("out"));
        tempDir = Files.createTempDirectory(Path.of("out"), "file-catalogo-repository-test-");
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
    void load_conFileAssente_restituisceCatalogoVuoto() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        Catalogo catalogo = repository.load();

        assertTrue(catalogo.getCategorie().isEmpty());
    }

    @Test
    void load_quandoInvocatoDueVolte_restituisceIstanzeDistinte() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        assertNotSame(repository.load(), repository.load());
    }

    @Test
    void save_conCatalogoVuoto_creaFile() {
        Path path = tempDir.resolve("catalogo.json");

        FileCatalogoRepository repository = new FileCatalogoRepository(path);
        repository.save(new Catalogo());

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conCatalogoModificato_persisteLaCategoria() {
        Path path = tempDir.resolve("catalogo.json");

        FileCatalogoRepository repository = new FileCatalogoRepository(path);
        Catalogo catalogo = repository.load();
        catalogo.addCategoria("Sport");
        repository.save(catalogo);

        Catalogo ricaricato = new FileCatalogoRepository(path).load();

        assertEquals("Sport", ricaricato.getCategorie().get(0).getNome());
    }

    @Test
    void costruttore_conPathNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FileCatalogoRepository(null));
    }

    @Test
    void save_conCatalogoNull_lanciaNullPointerException() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        assertThrows(NullPointerException.class, () -> repository.save(null));
    }
}
