package it.unibs.ingsoft.domain.utente;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FruitoreTest {
    @Test
    void costruttore_conUsernameValidoTrimmato_salvaUsernameSenzaSpaziEsterni() {
        Fruitore fruitore = new Fruitore("  mario  ");

        assertEquals("mario", fruitore.getUsername());
    }

    @Test
    void costruttore_conUsernameNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class, () -> new Fruitore(null));

        assertInstanceOf(UserFailure.UsernameInvalid.class, exception.failure());
    }

    @Test
    void costruttore_conUsernameBlank_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class, () -> new Fruitore("   "));

        assertInstanceOf(UserFailure.UsernameInvalid.class, exception.failure());
    }

    @Test
    void equals_conStessaIstanza_restituisceTrue() {
        Fruitore fruitore = new Fruitore("mario");

        assertEquals(fruitore, fruitore);
    }

    @Test
    void equals_conFruitoriConStessoUsername_restituisceTrue() {
        assertEquals(new Fruitore("mario"), new Fruitore("mario"));
    }

    @Test
    void equals_conFruitoriConUsernameDiverso_restituisceFalse() {
        assertNotEquals(new Fruitore("mario"), new Fruitore("luigi"));
    }

    @Test
    void equals_conOggettoNull_restituisceFalse() {
        Fruitore fruitore = new Fruitore("mario");

        assertNotEquals(null, fruitore);
    }

    @Test
    void equals_conConfiguratoreConStessoUsername_restituisceFalse() {
        assertNotEquals(new Fruitore("mario"), new Configuratore("mario"));
    }

    @Test
    void hashCode_conFruitoriConStessoUsername_restituisceStessoHashCode() {
        assertEquals(new Fruitore("mario").hashCode(), new Fruitore("mario").hashCode());
    }

    @Test
    void toString_restituisceClasseEUsername() {
        assertEquals("Fruitore[mario]", new Fruitore("mario").toString());
    }
}
