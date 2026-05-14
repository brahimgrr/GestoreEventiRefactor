package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FileRepositoryFactoryTest {
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of("out"));
        tempDir = Files.createTempDirectory(Path.of("out"), "file-repository-factory-test-");
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
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() throws Exception {
        resetSingleton();

        assertSame(FileRepositoryFactory.getInstance(), FileRepositoryFactory.getInstance());
    }

    @Test
    void createCatalogoRepository_quandoInvocato_restituisceRepositoryCatalogoSuFile() {
        ICatalogoRepository repository = FileRepositoryFactory.getInstance().createCatalogoRepository();

        assertInstanceOf(FileCatalogoRepository.class, repository);
    }

    @Test
    void createCredenzialiRepository_quandoInvocato_restituisceRepositoryCredenzialiSuFile() {
        ICredenzialiRepository repository = FileRepositoryFactory.getInstance().createCredenzialiRepository();

        assertInstanceOf(FileCredenzialiRepository.class, repository);
    }

    @Test
    void createBachecaRepository_quandoInvocato_restituisceRepositoryBachecaSuFile() {
        IBachecaRepository repository = FileRepositoryFactory.getInstance().createBachecaRepository();

        assertInstanceOf(FileBachecaRepository.class, repository);
    }

    @Test
    void createSpazioPersonaleRepository_quandoInvocato_restituisceRepositorySpazioPersonaleSuFile() {
        ISpazioPersonaleRepository repository = FileRepositoryFactory.getInstance().createSpazioPersonaleRepository();

        assertInstanceOf(FileSpazioPersonaleRepository.class, repository);
    }

    @Test
    void costruttore_conDataDirNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FileRepositoryFactory(null));
    }

    @Test
    void repositoryCreateConDataDirCustom_salvanoNeiFileAttesi() {
        FileRepositoryFactory factory = new FileRepositoryFactory(tempDir);

        factory.createCatalogoRepository().save(new it.unibs.ingsoft.domain.Catalogo());
        factory.createCredenzialiRepository().save(new it.unibs.ingsoft.domain.Credenziali());
        factory.createBachecaRepository().save(new it.unibs.ingsoft.domain.Bacheca());
        factory.createSpazioPersonaleRepository().save(new it.unibs.ingsoft.domain.ArchivioNotifiche());

        assertAll(
                () -> assertTrue(Files.exists(tempDir.resolve("catalogo.json"))),
                () -> assertTrue(Files.exists(tempDir.resolve("utenti.json"))),
                () -> assertTrue(Files.exists(tempDir.resolve("proposte.json"))),
                () -> assertTrue(Files.exists(tempDir.resolve("notifiche.json")))
        );
    }

    private void resetSingleton() throws Exception {
        Field instance = FileRepositoryFactory.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
