package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.application.authentication.PasswordHasher;
import it.unibs.ingsoft.domain.model.utente.UserAccount;
import it.unibs.ingsoft.domain.model.utente.UserRole;
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
        PasswordHasher hasher = PasswordHasher.pbkdf2();
        FileUserRepository repository = new FileUserRepository(path);
        UserAccount account = UserAccount.create("Mario", UserRole.FRUITORE, hasher.hash("pass1234"));

        repository.save(account);

        UserAccount loaded = repository.findByUsername("mario").orElseThrow();
        String json = Files.readString(path);
        assertAll(
                () -> assertTrue(repository.existsByUsername("MARIO")),
                () -> assertTrue(hasher.matches("pass1234", loaded.passwordHash())),
                () -> assertFalse(json.contains("pass1234")),
                () -> assertTrue(json.contains("PBKDF2WithHmacSHA256"))
        );
    }
}
