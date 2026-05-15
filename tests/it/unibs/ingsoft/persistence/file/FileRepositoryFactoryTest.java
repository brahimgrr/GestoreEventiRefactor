package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.repository.CatalogoRepository;
import it.unibs.ingsoft.domain.repository.NotificationRepository;
import it.unibs.ingsoft.domain.repository.PropostaRepository;
import it.unibs.ingsoft.domain.repository.UserRepository;
import it.unibs.ingsoft.domain.model.catalogo.Catalogo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.utente.PasswordHash;
import it.unibs.ingsoft.domain.model.utente.UserAccount;
import it.unibs.ingsoft.domain.model.utente.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

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
        CatalogoRepository repository = FileRepositoryFactory.getInstance().createCatalogoRepository();

        assertInstanceOf(FileCatalogoRepository.class, repository);
    }

    @Test
    void createUserRepository_quandoInvocato_restituisceRepositoryUtentiSuFile() {
        UserRepository repository = FileRepositoryFactory.getInstance().createUserRepository();

        assertInstanceOf(FileUserRepository.class, repository);
    }

    @Test
    void createPropostaRepository_quandoInvocato_restituisceRepositoryProposteSuFile() {
        PropostaRepository repository = FileRepositoryFactory.getInstance().createPropostaRepository();

        assertInstanceOf(FilePropostaRepository.class, repository);
    }

    @Test
    void createNotificationRepository_quandoInvocato_restituisceRepositoryNotificheSuFile() {
        NotificationRepository repository = FileRepositoryFactory.getInstance().createNotificationRepository();

        assertInstanceOf(FileNotificationRepository.class, repository);
    }

    @Test
    void costruttore_conDataDirNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FileRepositoryFactory(null));
    }

    @Test
    void repositoryCreateConDataDirCustom_salvanoNeiFileAttesi() {
        FileRepositoryFactory factory = new FileRepositoryFactory(tempDir);

        factory.createCatalogoRepository().save(new Catalogo());
        factory.createUserRepository().save(UserAccount.create(
                "mario",
                UserRole.FRUITORE,
                new PasswordHash("PBKDF2WithHmacSHA256", 1, "salt", "hash")));
        factory.createPropostaRepository().save(new Proposta(new Categoria("Sport"), List.of(), List.of()));
        factory.createNotificationRepository().add("mario", new Notifica("messaggio"));

        assertAll(
                () -> assertTrue(Files.exists(tempDir.resolve("catalogo.json"))),
                () -> assertTrue(Files.exists(tempDir.resolve("users.json"))),
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
