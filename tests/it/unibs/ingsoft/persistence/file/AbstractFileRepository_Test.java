package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.utente.Credenziali;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AbstractFileRepository_Test {
    @TempDir
    Path tempDir;

    @Test
    void load_conFileAssente_restituisceValoreDiDefault() {
        TestCredenzialiRepository repository = new TestCredenzialiRepository(tempDir.resolve("missing.json"));

        Credenziali credenziali = repository.loadDirect();

        assertTrue(credenziali.getConfiguratori().isEmpty());
    }

    /*
    DA WARNING ma va a buon fine
     */
    @Test
    void load_conJsonMalformed_restituisceValoreDiDefault() throws Exception {
        Path path = tempDir.resolve("broken.json");
        Files.writeString(path, "{ json non valido");
        TestCredenzialiRepository repository = new TestCredenzialiRepository(path);

        Credenziali credenziali = repository.loadDirect();

        assertTrue(credenziali.getFruitori().isEmpty());
    }

    @Test
    void save_conParentDirectoryAssente_creaDirectoryEFileJson() {
        Path path = tempDir.resolve("nested").resolve("credenziali.json");
        TestCredenzialiRepository repository = new TestCredenzialiRepository(path);
        Credenziali credenziali = new Credenziali();
        credenziali.addConfiguratore("Admin", "pwd");

        repository.saveDirect(credenziali);

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conDatoNull_lanciaNullPointerException() {
        TestCredenzialiRepository repository = new TestCredenzialiRepository(tempDir.resolve("credenziali.json"));

        assertThrows(NullPointerException.class, () -> repository.saveDirect(null));
    }

    private static final class TestCredenzialiRepository extends AbstractFileRepository<Credenziali> {
        private TestCredenzialiRepository(Path path) {
            super(path, Credenziali.class, Credenziali::new);
        }

        private Credenziali loadDirect() {
            return load();
        }

        private void saveDirect(Credenziali credenziali) {
            save(credenziali);
        }
    }
}
