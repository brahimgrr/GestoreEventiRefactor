package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.notifica.ArchivioNotifiche;
import it.unibs.ingsoft.domain.utente.SpazioPersonale;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArchivioNotificheTest {
    @Test
    void getSpazioDi_conUsernameNuovo_creaSpazioPersonaleVuoto() {
        ArchivioNotifiche archivio = new ArchivioNotifiche();

        SpazioPersonale spazio = archivio.getSpazioDi("mario");

        assertAll(
                () -> assertNotNull(spazio),
                () -> assertTrue(spazio.getNotifiche().isEmpty()),
                () -> assertSame(spazio, archivio.getUtenti().get("mario"))
        );
    }

    @Test
    void getSpazioDi_conUsernameGiaPresente_restituisceLaStessaIstanza() {
        ArchivioNotifiche archivio = new ArchivioNotifiche();

        SpazioPersonale primoAccesso = archivio.getSpazioDi("mario");
        SpazioPersonale secondoAccesso = archivio.getSpazioDi("mario");

        assertSame(primoAccesso, secondoAccesso);
    }

    @Test
    void fromJson_conMappaNull_creaArchivioSenzaUtenti() {
        ArchivioNotifiche archivio = ArchivioNotifiche.fromJson(null);

        assertTrue(archivio.getUtenti().isEmpty());
    }

    @Test
    void fromJson_conMappaValorizzata_copiaGliUtentiNelNuovoArchivio() {
        SpazioPersonale spazio = new SpazioPersonale();

        ArchivioNotifiche archivio = ArchivioNotifiche.fromJson(Map.of("mario", spazio));

        assertSame(spazio, archivio.getUtenti().get("mario"));
    }
}
