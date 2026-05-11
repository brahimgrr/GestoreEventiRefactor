package it.unibs.ingsoft.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CredenzialiTest {
    @Test
    void addConfiguratore_conUsernameMaiuscoloESpazi_salvaChiaveMinuscolaTrimmata() {
        Credenziali credenziali = new Credenziali();

        credenziali.addConfiguratore("  Admin  ", "pwd");

        assertEquals("pwd", credenziali.getConfiguratori().get("admin"));
    }

    @Test
    void addFruitore_conUsernameMaiuscoloESpazi_salvaChiaveMinuscolaTrimmata() {
        Credenziali credenziali = new Credenziali();

        credenziali.addFruitore("  Mario  ", "pwd");

        assertEquals("pwd", credenziali.getFruitori().get("mario"));
    }

    @Test
    void getConfiguratori_quandoSiModificaMappaRestituita_lanciaUnsupportedOperationException() {
        Credenziali credenziali = new Credenziali();

        assertThrows(UnsupportedOperationException.class,
                () -> credenziali.getConfiguratori().put("admin", "pwd"));
    }

    @Test
    void getFruitori_quandoSiModificaMappaRestituita_lanciaUnsupportedOperationException() {
        Credenziali credenziali = new Credenziali();

        assertThrows(UnsupportedOperationException.class,
                () -> credenziali.getFruitori().put("mario", "pwd"));
    }

    @Test
    void fromJson_conMappeNull_creaCredenzialiVuote() {
        Credenziali credenziali = Credenziali.fromJson(null, null);

        assertAll(
                () -> assertTrue(credenziali.getConfiguratori().isEmpty()),
                () -> assertTrue(credenziali.getFruitori().isEmpty())
        );
    }

    /*
    NON DOVREBBE FUNZIONARE COSI
     */
    @Test
    void fromJson_conMappeValorizzate_copiaConfiguratoriEFruitori() {
        Credenziali credenziali = Credenziali.fromJson(Map.of("admin", "a"), Map.of("admin", "m"));

        assertAll(
                () -> assertEquals("a", credenziali.getConfiguratori().get("admin")),
                () -> assertEquals("m", credenziali.getFruitori().get("admin"))
        );
    }
}
