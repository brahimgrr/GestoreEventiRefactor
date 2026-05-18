package it.unibs.ingsoft.persistence.dto;

import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CredenzialiDTOTest {
    @Test
    void addConfiguratore_conUsernameMaiuscoloESpazi_salvaChiaveMinuscolaTrimmata() {
        CredenzialiDTO credenziali = new CredenzialiDTO();

        credenziali.addConfiguratore("  Admin  ", "pwd");

        assertEquals("pwd", credenziali.getConfiguratori().get("admin"));
    }

    @Test
    void addFruitore_conUsernameMaiuscoloESpazi_salvaChiaveMinuscolaTrimmata() {
        CredenzialiDTO credenziali = new CredenzialiDTO();

        credenziali.addFruitore("  Mario  ", "pwd");

        assertEquals("pwd", credenziali.getFruitori().get("mario"));
    }

    @Test
    void getConfiguratori_quandoSiModificaMappaRestituita_lanciaUnsupportedOperationException() {
        CredenzialiDTO credenziali = new CredenzialiDTO();

        assertThrows(UnsupportedOperationException.class,
                () -> credenziali.getConfiguratori().put("admin", "pwd"));
    }

    @Test
    void getFruitori_quandoSiModificaMappaRestituita_lanciaUnsupportedOperationException() {
        CredenzialiDTO credenziali = new CredenzialiDTO();

        assertThrows(UnsupportedOperationException.class,
                () -> credenziali.getFruitori().put("mario", "pwd"));
    }

    @Test
    void fromJson_conMappeNull_creaCredenzialiVuote() {
        CredenzialiDTO credenziali = CredenzialiDTO.fromJson(null, null);

        assertAll(
                () -> assertTrue(credenziali.getConfiguratori().isEmpty()),
                () -> assertTrue(credenziali.getFruitori().isEmpty())
        );
    }

    @Test
    void fromJson_conMappeValorizzate_copiaConfiguratoriEFruitori() {
        CredenzialiDTO credenziali = CredenzialiDTO.fromJson(Map.of("admin", "a"), Map.of("admin", "m"));

        assertAll(
                () -> assertEquals("a", credenziali.getConfiguratori().get("admin")),
                () -> assertEquals("m", credenziali.getFruitori().get("admin"))
        );
    }
}
