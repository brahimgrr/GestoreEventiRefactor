package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.utente.Configuratore;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.utente.UtenteFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtenteFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(UtenteFactory.getInstance(), UtenteFactory.getInstance());
    }

    @Test
    void creaConfiguratore_conUsernameValido_restituisceConfiguratoreConUsername() {
        Configuratore configuratore = UtenteFactory.getInstance().creaConfiguratore("admin");

        assertEquals("admin", configuratore.getUsername());
    }

    @Test
    void creaConfiguratore_conUsernameBlank_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> UtenteFactory.getInstance().creaConfiguratore("   "));
    }

    @Test
    void creaConfiguratore_conUsernameNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> UtenteFactory.getInstance().creaConfiguratore(null));
    }

    @Test
    void creaFruitore_conUsernameValido_restituisceFruitoreConUsername() {
        Fruitore fruitore = UtenteFactory.getInstance().creaFruitore("mario");

        assertEquals("mario", fruitore.getUsername());
    }

    @Test
    void creaFruitore_conUsernameBlank_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> UtenteFactory.getInstance().creaFruitore("   "));
    }

    @Test
    void creaFruitore_conUsernameNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> UtenteFactory.getInstance().creaFruitore(null));
    }
}
