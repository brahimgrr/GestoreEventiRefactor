package it.unibs.ingsoft.persistence.dto;

import it.unibs.ingsoft.persistence.dto.ArchivioNotificheDTO;
import it.unibs.ingsoft.persistence.dto.SpazioPersonaleDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArchivioNotificheDTOTest {
    @Test
    void getSpazioDi_conUsernameNuovo_creaSpazioPersonaleVuoto() {
        ArchivioNotificheDTO archivio = new ArchivioNotificheDTO();

        SpazioPersonaleDTO spazio = archivio.getSpazioDi("mario");

        assertAll(
                () -> assertNotNull(spazio),
                () -> assertTrue(spazio.getNotifiche().isEmpty()),
                () -> assertSame(spazio, archivio.getUtenti().get("mario"))
        );
    }

    @Test
    void getSpazioDi_conUsernameGiaPresente_restituisceLaStessaIstanza() {
        ArchivioNotificheDTO archivio = new ArchivioNotificheDTO();

        SpazioPersonaleDTO primoAccesso = archivio.getSpazioDi("mario");
        SpazioPersonaleDTO secondoAccesso = archivio.getSpazioDi("mario");

        assertSame(primoAccesso, secondoAccesso);
    }

    @Test
    void findSpazioDi_conUsernameNull_restituisceOptionalVuoto() {
        ArchivioNotificheDTO archivio = new ArchivioNotificheDTO();

        assertTrue(archivio.findSpazioDi(null).isEmpty());
    }

    @Test
    void findSpazioDi_conUsernameAssente_restituisceOptionalVuoto() {
        ArchivioNotificheDTO archivio = new ArchivioNotificheDTO();

        assertTrue(archivio.findSpazioDi("mario").isEmpty());
    }

    @Test
    void findSpazioDi_conUsernamePresente_restituisceSpazioPersonale() {
        ArchivioNotificheDTO archivio = new ArchivioNotificheDTO();
        SpazioPersonaleDTO spazio = archivio.getSpazioDi("mario");

        assertSame(spazio, archivio.findSpazioDi("mario").orElseThrow());
    }

    @Test
    void fromJson_conMappaNull_creaArchivioSenzaUtenti() {
        ArchivioNotificheDTO archivio = ArchivioNotificheDTO.fromJson(null);

        assertTrue(archivio.getUtenti().isEmpty());
    }

    @Test
    void fromJson_conMappaValorizzata_copiaGliUtentiNelNuovoArchivio() {
        SpazioPersonaleDTO spazio = new SpazioPersonaleDTO();

        ArchivioNotificheDTO archivio = ArchivioNotificheDTO.fromJson(Map.of("mario", spazio));

        assertSame(spazio, archivio.getUtenti().get("mario"));
    }
}
