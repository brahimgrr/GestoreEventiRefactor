package it.unibs.ingsoft.domain;

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
        assertThrows(IllegalStateException.class, () -> new Fruitore(null));
    }

    @Test
    void costruttore_conUsernameBlank_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> new Fruitore("   "));
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
}
