package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.model.utente.PasswordHash;
import it.unibs.ingsoft.domain.model.utente.UserAccount;
import it.unibs.ingsoft.domain.model.utente.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAccountTest {
    @Test
    void create_normalizzaUsernamePerLookupMaMantieneFormaVisuale() {
        PasswordHash hash = new PasswordHash("hash");

        UserAccount account = UserAccount.create("  Mario.Rossi  ", UserRole.FRUITORE, hash);

        assertEquals("Mario.Rossi", account.username());
        assertEquals("mario.rossi", account.normalizedUsername());
        assertEquals(UserRole.FRUITORE, account.role());
        assertEquals(hash, account.passwordHash());
    }

    @Test
    void create_conDatiInvalidi_lanciaNullPointerExceptionOIllegalArgumentException() {
        PasswordHash hash = new PasswordHash("hash");

        assertThrows(IllegalArgumentException.class, () -> UserAccount.create(" ", UserRole.FRUITORE, hash));
        assertThrows(NullPointerException.class, () -> UserAccount.create("mario", null, hash));
        assertThrows(NullPointerException.class, () -> UserAccount.create("mario", UserRole.FRUITORE, null));
    }
}
