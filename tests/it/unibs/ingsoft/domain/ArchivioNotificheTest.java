package it.unibs.ingsoft.domain;

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
    void findSpazioDi_conUsernameNull_restituisceOptionalVuoto() {
        ArchivioNotifiche archivio = new ArchivioNotifiche();

        assertTrue(archivio.findSpazioDi(null).isEmpty());
    }

    @Test
    void findSpazioDi_conUsernameAssente_restituisceOptionalVuoto() {
        ArchivioNotifiche archivio = new ArchivioNotifiche();

        assertTrue(archivio.findSpazioDi("mario").isEmpty());
    }

    @Test
    void findSpazioDi_conUsernamePresente_restituisceSpazioPersonale() {
        ArchivioNotifiche archivio = new ArchivioNotifiche();
        SpazioPersonale spazio = archivio.getSpazioDi("mario");

        assertSame(spazio, archivio.findSpazioDi("mario").orElseThrow());
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
