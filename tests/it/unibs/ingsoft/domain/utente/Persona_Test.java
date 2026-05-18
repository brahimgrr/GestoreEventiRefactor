package it.unibs.ingsoft.domain.utente;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/*
Classe astratta. secondo me non ha senso testarla ma ja piu senso
testare configuratore e fruitore che la estendono
 */
class Persona_Test {
    @Test
    void costruttore_conUsernameNullOBlank_lanciaUsernameInvalid() {
        DomainException nullException = assertThrows(DomainException.class, () -> new Configuratore(null));
        DomainException blankException = assertThrows(DomainException.class, () -> new Fruitore("   "));

        assertAll(
                () -> assertInstanceOf(UserFailure.UsernameInvalid.class, nullException.failure()),
                () -> assertInstanceOf(UserFailure.UsernameInvalid.class, blankException.failure())
        );
    }

    @Test
    void costruttore_conUsernameValido_trimaUsername() {
        Persona persona = new Configuratore("  admin  ");

        assertEquals("admin", persona.getUsername());
    }

    @Test
    void equals_copreStessaIstanzaNullClasseDiversaEUsernameDiverso() {
        Persona admin = new Configuratore("admin");
        Persona admin2 = new Configuratore("admin");
        Persona altroAdmin = new Fruitore("admin");
        Persona mario = new Configuratore("mario");

        assertAll(
                () -> assertEquals(admin, admin),
                () -> assertEquals(admin, admin2),
                () -> assertNotEquals(admin, null),
                () -> assertNotEquals(admin, "admin"),
                () -> assertNotEquals(admin, altroAdmin),
                () -> assertNotEquals(admin, mario)
        );
    }

    @Test
    void hashCodeEToString_usanoUsernameEClasseConcreta() {
        Persona admin = new Configuratore("admin");

        assertAll(
                () -> assertEquals("admin".hashCode(), admin.hashCode()),
                () -> assertEquals("Configuratore[admin]", admin.toString())
        );
    }
}
