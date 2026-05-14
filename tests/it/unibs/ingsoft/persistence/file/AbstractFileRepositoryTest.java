package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class AbstractFileRepositoryTest {
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of("out"));
        tempDir = Files.createTempDirectory(Path.of("out"), "abstract-file-repository-test-");
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
    void load_conFileAssente_restituisceValoreDiDefault() {
        TestCredenzialiRepository repository = new TestCredenzialiRepository(tempDir.resolve("missing.json"));

        CredenzialiDTO credenziali = repository.loadDirect();

        assertTrue(credenziali.getConfiguratori().isEmpty());
    }

    @Test
    void load_conJsonMalformed_lanciaPersistenceException() throws Exception {
        Path path = tempDir.resolve("broken.json");
        Files.writeString(path, "{ json non valido");

        TestCredenzialiRepository repository = new TestCredenzialiRepository(path);

        assertThrows(PersistenceException.class, repository::loadDirect);
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

    @Test
    void costruttore_conTipoNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new TestNullConstructorRepository(tempDir.resolve("dati.json"), null, CredenzialiDTO::new));
    }

    @Test
    void costruttore_conDefaultValueNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new TestNullConstructorRepository(tempDir.resolve("dati.json"), CredenzialiDTO.class, null));
    }

    @Test
    void saveELoad_conDateSerializzaEDeserializzaFormatoCustom() {
        Path path = tempDir.resolve("dated.json");
        TestDatedRepository repository = new TestDatedRepository(path);
        DatedData data = new DatedData(
                LocalDate.of(2026, 5, 13),
                LocalDateTime.of(2026, 5, 13, 14, 30, 15));

        repository.saveDirect(data);
        DatedData loaded = repository.loadDirect();

        assertAll(
                () -> assertTrue(Files.readString(path).contains("13/05/2026")),
                () -> assertTrue(Files.readString(path).contains("13/05/2026 14:30:15")),
                () -> assertEquals(data.date, loaded.date),
                () -> assertEquals(data.dateTime, loaded.dateTime)
        );
    }

    @Test
    void save_conPathSenzaParent_salvaFile() throws Exception {
        Path path = Path.of("abstract-file-repository-parentless-test.json");
        TestCredenzialiRepository repository = new TestCredenzialiRepository(path);

        try {
            repository.saveDirect(new CredenzialiDTO());

            assertTrue(Files.exists(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void save_quandoMoveAtomicaNonSupportata_usaMoveReplacing() {
        Path path = tempDir.resolve("fallback.json");
        FallbackMoveRepository repository = new FallbackMoveRepository(path);

        repository.saveDirect(new CredenzialiDTO());

        assertAll(
                () -> assertTrue(repository.moveReplacingCalled),
                () -> assertTrue(Files.exists(path))
        );
    }

    @Test
    void save_quandoMoveReplacingFallisce_lanciaPersistenceException() {
        Path path = tempDir.resolve("failing.json");
        FailingFallbackMoveRepository repository = new FailingFallbackMoveRepository(path);

        assertThrows(PersistenceException.class, () -> repository.saveDirect(new CredenzialiDTO()));
    }

    private static class TestCredenzialiRepository extends AbstractFileRepository<CredenzialiDTO> {
        private TestCredenzialiRepository(Path path) {
            super(path, CredenzialiDTO.class, CredenzialiDTO::new);
        }

        private CredenzialiDTO loadDirect() {
            return load();
        }

        protected void saveDirect(CredenzialiDTO credenziali) {
            save(credenziali);
        }
    }

    public static final class DatedData {
        public LocalDate date;
        public LocalDateTime dateTime;

        public DatedData() {
        }

        private DatedData(LocalDate date, LocalDateTime dateTime) {
            this.date = date;
            this.dateTime = dateTime;
        }
    }

    private static final class TestDatedRepository extends AbstractFileRepository<DatedData> {
        private TestDatedRepository(Path path) {
            super(path, DatedData.class, DatedData::new);
        }

        private DatedData loadDirect() {
            return load();
        }

        private void saveDirect(DatedData data) {
            save(data);
        }
    }

    private static class FallbackMoveRepository extends TestCredenzialiRepository {
        private boolean moveReplacingCalled;

        private FallbackMoveRepository(Path path) {
            super(path);
        }

        @Override
        protected void moveAtomically(Path tmp, Path target) throws IOException {
            throw new AtomicMoveNotSupportedException(tmp.toString(), target.toString(), "test");
        }

        @Override
        protected void moveReplacing(Path tmp, Path target) throws IOException {
            moveReplacingCalled = true;
            super.moveReplacing(tmp, target);
        }
    }

    private static final class FailingFallbackMoveRepository extends FallbackMoveRepository {
        private FailingFallbackMoveRepository(Path path) {
            super(path);
        }

        @Override
        protected void moveReplacing(Path tmp, Path target) throws IOException {
            throw new IOException("forced failure");
        }
    }

    private static final class TestNullConstructorRepository extends AbstractFileRepository<CredenzialiDTO> {
        private TestNullConstructorRepository(Path path, Class<CredenzialiDTO> type,
                                              java.util.function.Supplier<CredenzialiDTO> defaultValue) {
            super(path, type, defaultValue);
        }
    }
}
