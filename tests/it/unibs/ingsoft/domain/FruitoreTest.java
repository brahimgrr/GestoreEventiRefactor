package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.utente.Fruitore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FruitoreTest {
    @Test
    void costruttore_conUsernameValidoTrimmato_salvaUsernameSenzaSpaziEsterni() {
        Fruitore fruitore = new Fruitore("  mario  ");

        assertEquals("mario", fruitore.getUsername());
    }

    @Test
    void costruttore_conUsernameNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Fruitore(null));
    }

    @Test
    void costruttore_conUsernameBlank_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Fruitore("   "));
    }


    @Test
    void hashCode_conFruitoriConStessoUsername_restituisceStessoHashCode() {
        assertEquals(new Fruitore("mario").hashCode(), new Fruitore("mario").hashCode());
    }
}
