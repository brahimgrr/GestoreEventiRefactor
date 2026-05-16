package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.domain.model.utente.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUserRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void saveEFindByUsername_persistonoHashSenzaPasswordInChiaro() throws Exception {
        Path path = tempDir.resolve("users.json");
        FileUserRepository repository = new FileUserRepository(path);
        AuthenticationService service = new AuthenticationService(repository);

        service.registraNuovoFruitore("Mario", "pass1234");

        UserAccount loaded = repository.findByUsername("mario").orElseThrow();
        String json = Files.readString(path);
        assertAll(
                () -> assertTrue(repository.existsByUsername("MARIO")),
                () -> assertTrue(service.loginFruitore("mario", "pass1234").isPresent()),
                () -> assertFalse(json.contains("pass1234")),
                () -> assertFalse(json.contains("algorithm")),
                () -> assertFalse(json.contains("iterations")),
                () -> assertFalse(json.contains("salt")),
                () -> assertFalse(loaded.passwordHash().hash().isBlank())
        );
    }
}
