package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.utente.Configuratore;
import it.unibs.ingsoft.domain.utente.Fruitore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratoreTest {
    @Test
    void costruttore_conUsernameValidoTrimmato_salvaUsernameSenzaSpaziEsterni() {
        Configuratore configuratore = new Configuratore("  admin  ");

        assertEquals("admin", configuratore.getUsername());
    }

    @Test
    void costruttore_conUsernameNull_lanciaIllegalStateException() {
        assertThrows(DomainException.class, () -> new Configuratore(null));
    }

    @Test
    void costruttore_conUsernameBlank_lanciaIllegalStateException() {
        assertThrows(DomainException.class, () -> new Configuratore("  "));
    }

    @Test
    void equals_conConfiguratoriConStessoUsername_restituisceTrue() {
        assertEquals(new Configuratore("admin"), new Configuratore("admin"));
    }

    @Test
    void equals_conStessaIstanza_restituisceTrue() {
        Configuratore configuratore = new Configuratore("admin");

        assertEquals(configuratore, configuratore);
    }

    @Test
    void equals_conOggettoNull_restituisceFalse() {
        Configuratore configuratore = new Configuratore("admin");

        assertNotEquals(null, configuratore);
    }

    @Test
    void equals_conFruitoreConStessoUsername_restituisceFalse() {
        assertNotEquals(new Configuratore("utente"), new Fruitore("utente"));
    }

    @Test
    void hashCode_conStessoUsername_restituisceStessoHashCode() {
        assertEquals(new Configuratore("admin").hashCode(), new Configuratore("admin").hashCode());
    }
}
