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
    void hashCode_conFruitoriConStessoUsername_restituisceStessoHashCode() {
        assertEquals(new Fruitore("mario").hashCode(), new Fruitore("mario").hashCode());
    }
}
