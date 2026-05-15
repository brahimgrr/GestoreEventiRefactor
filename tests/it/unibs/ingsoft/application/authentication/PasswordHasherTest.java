package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.domain.model.utente.PasswordHash;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test
    void hash_nonContienePasswordInChiaroEVerificaSoloPasswordCorretta() {
        PasswordHasher hasher = PasswordHasher.pbkdf2();

        PasswordHash hash = hasher.hash("pass1234");

        assertNotEquals("pass1234", hash.hash());
        assertTrue(hasher.matches("pass1234", hash));
        assertFalse(hasher.matches("sbagliata", hash));
    }
}
