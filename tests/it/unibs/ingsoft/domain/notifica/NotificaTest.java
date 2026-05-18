package it.unibs.ingsoft.domain.notifica;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificaTest {
    @Test
    void costruttoreConMessaggio_quandoInvocato_creaIdNonNullEDataCreazioneNonNull() {
        Notifica notifica = new Notifica("messaggio");

        assertAll(
                () -> assertNotNull(notifica.id()),
                () -> assertEquals("messaggio", notifica.messaggio()),
                () -> assertEquals(LocalDateTime.now().toLocalDate(), notifica.dataCreazione().toLocalDate())
        );
    }

    @Test
    void equals_conStessoId_restituisceTrueAncheSeMessaggioDiverso() {
        LocalDateTime data = LocalDateTime.now();

        assertEquals(notifica("id-1", "uno", data), notifica("id-1", "due", data.plusDays(1)));
    }

    @Test
    void equals_conIdDiverso_restituisceFalse() {
        LocalDateTime data = LocalDateTime.now();

        assertNotEquals(notifica("id-1", "uno", data), notifica("id-2", "uno", data));
    }

    @Test
    void equals_conOggettoNull_restituisceFalse() {
        Notifica notifica = notifica("id-1", "uno", LocalDateTime.now());

        assertFalse(notifica.equals(null));
    }

    @Test
    void equals_conOggettoDiClasseDiversa_restituisceFalse() {
        Notifica notifica = notifica("id-1", "uno", LocalDateTime.now());

        assertFalse(notifica.equals("id-1"));
    }

    @Test
    void equals_conOggettoNonNullStessaClasseEIdDiverso_restituisceFalse() {
        LocalDateTime data = LocalDateTime.now();

        assertFalse(notifica("id-1", "uno", data).equals(notifica("id-2", "due", data)));
    }

    @Test
    void hashCode_conStessoId_restituisceStessoHashCode() {
        LocalDateTime data = LocalDateTime.now();

        assertEquals(notifica("id-1", "uno", data).hashCode(),
                notifica("id-1", "due", data.plusDays(1)).hashCode());
    }

    @Test
    void hashCode_conDiversoId_restituisceHashCodeDiverso() {
        LocalDateTime data = LocalDateTime.now();

        assertNotEquals(notifica("id-1", "uno", data).hashCode(),
                notifica("id-2", "due", data.plusDays(1)).hashCode());
    }

    @Test
    void costruttoreCompleto_conValoriNull_impostaDefaultECopiaPayloadVuoto() {
        Notifica notifica = new Notifica(null, null, null, "messaggio", null);

        assertAll(
                () -> assertNotNull(notifica.id()),
                () -> assertEquals(NotificaType.LEGACY_MESSAGGIO, notifica.type()),
                () -> assertTrue(notifica.payload().isEmpty()),
                () -> assertEquals("messaggio", notifica.messaggio()),
                () -> assertNotNull(notifica.dataCreazione())
        );
    }

    @Test
    void costruttoreCompleto_conValoriPresenti_mantieneValoriECopiaPayload() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 15, 10, 30);
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("titolo", "Torneo");

        Notifica notifica = new Notifica(
                "id-1",
                NotificaType.PROPOSTA_CONFERMATA,
                payload,
                "messaggio",
                data);
        payload.put("titolo", "Modificato");

        assertAll(
                () -> assertEquals("id-1", notifica.id()),
                () -> assertEquals(NotificaType.PROPOSTA_CONFERMATA, notifica.type()),
                () -> assertEquals("Torneo", notifica.payload().get("titolo")),
                () -> assertEquals("messaggio", notifica.messaggio()),
                () -> assertEquals(data, notifica.dataCreazione())
        );
    }

    @Test
    void costruttoreCompatibile_conIdMessaggioEData_usaLegacyPayloadVuotoEDataIndicata() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 15, 11, 0);

        Notifica notifica = new Notifica("id-compatibile", "messaggio", data);

        assertAll(
                () -> assertEquals("id-compatibile", notifica.id()),
                () -> assertEquals(NotificaType.LEGACY_MESSAGGIO, notifica.type()),
                () -> assertTrue(notifica.payload().isEmpty()),
                () -> assertEquals("messaggio", notifica.messaggio()),
                () -> assertEquals(data, notifica.dataCreazione())
        );
    }

    @Test
    void notificaStrutturata_copiaPayloadEUsaTipoRichiesto() {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("titolo", "Torneo");

        Notifica notifica = Notifica.notificaStrutturata(NotificaType.PROPOSTA_RITIRATA, payload);

        assertAll(
                () -> assertEquals(NotificaType.PROPOSTA_RITIRATA, notifica.type()),
                () -> assertEquals("Torneo", notifica.payload().get("titolo")),
                () -> assertThrows(UnsupportedOperationException.class, () -> notifica.payload().put("x", "y"))
        );
    }

    @Test
    void notificaStrutturata_conPayloadNull_usaPayloadVuoto() {
        Notifica notifica = Notifica.notificaStrutturata(NotificaType.PROPOSTA_ANNULLATA, null);

        assertAll(
                () -> assertNotNull(notifica.id()),
                () -> assertEquals(NotificaType.PROPOSTA_ANNULLATA, notifica.type()),
                () -> assertTrue(notifica.payload().isEmpty()),
                () -> assertNull(notifica.messaggio()),
                () -> assertNotNull(notifica.dataCreazione())
        );
    }

    @Test
    void notificaStrutturata_conTipoNull_usaLegacyMessaggio() {
        Notifica notifica = Notifica.notificaStrutturata(null, Map.of("titolo", "Torneo"));

        assertAll(
                () -> assertEquals(NotificaType.LEGACY_MESSAGGIO, notifica.type()),
                () -> assertEquals("Torneo", notifica.payload().get("titolo"))
        );
    }

    @Test
    void equals_conStessoOggettoNullEClasseDiversa_copreRamiEquals() {
        Notifica notifica = new Notifica("id", NotificaType.LEGACY_MESSAGGIO, Map.of(), "m", LocalDateTime.now());

        assertAll(
                () -> assertEquals(notifica, notifica),
                () -> assertNotEquals(null, notifica),
                () -> assertNotEquals("id", notifica)
        );
    }

    @Test
    void recordAccessors_restituisconoIValoriCostruiti() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 15, 12, 0);
        Map<String, String> payload = Map.of("chiave", "valore");

        Notifica notifica = new Notifica(
                "id",
                NotificaType.PROPOSTA_RITIRATA,
                payload,
                "messaggio",
                data);

        assertAll(
                () -> assertEquals("id", notifica.id()),
                () -> assertEquals(NotificaType.PROPOSTA_RITIRATA, notifica.type()),
                () -> assertEquals(payload, notifica.payload()),
                () -> assertEquals("messaggio", notifica.messaggio()),
                () -> assertEquals(data, notifica.dataCreazione())
        );
    }

    private Notifica notifica(String id, String messaggio, LocalDateTime data) {
        return new Notifica(id, NotificaType.LEGACY_MESSAGGIO, Map.of(), messaggio, data);
    }
}
