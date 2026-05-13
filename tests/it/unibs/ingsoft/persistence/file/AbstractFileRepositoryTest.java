package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AbstractFileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void load_conFileAssente_restituisceValoreDiDefault() {
        TestCredenzialiRepository repository = new TestCredenzialiRepository(tempDir.resolve("missing.json"));

        CredenzialiDTO credenziali = repository.loadDirect();

        assertTrue(credenziali.getConfiguratori().isEmpty());
    }

    @Test
    void load_conJsonMalformed_lanciaUncheckedIOException() throws Exception {
        Path path = tempDir.resolve("broken.json");
        Files.writeString(path, "{ json non valido");

        TestCredenzialiRepository repository = new TestCredenzialiRepository(path);

        assertThrows(java.io.UncheckedIOException.class, repository::loadDirect);
    }

    @Test
    void save_conParentDirectoryAssente_creaDirectoryEFileJson() {
        Path path = tempDir.resolve("nested").resolve("credenziali.json");

        TestCredenzialiRepository repository = new TestCredenzialiRepository(path);

        CredenzialiDTO credenziali = new CredenzialiDTO();
        credenziali.addConfiguratore("Admin", "pwd");

        repository.saveDirect(credenziali);

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conDatoNull_lanciaNullPointerException() {
        TestCredenzialiRepository repository = new TestCredenzialiRepository(tempDir.resolve("credenziali.json"));

        assertThrows(NullPointerException.class, () -> repository.saveDirect(null));
    }

    private static final class TestCredenzialiRepository extends AbstractFileRepository<CredenzialiDTO> {
        private TestCredenzialiRepository(Path path) {
            super(path, CredenzialiDTO.class, CredenzialiDTO::new);
        }

        private CredenzialiDTO loadDirect() {
            return load();
        }

        private void saveDirect(CredenzialiDTO credenziali) {
            save(credenziali);
        }
    }
}
