package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaType;
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
    void equals_conStessoOggettoNullEClasseDiversa_copreRamiEquals() {
        Notifica notifica = new Notifica("id", NotificaType.LEGACY_MESSAGGIO, Map.of(), "m", LocalDateTime.now());

        assertAll(
                () -> assertEquals(notifica, notifica),
                () -> assertNotEquals(null, notifica),
                () -> assertNotEquals("id", notifica)
        );
    }

    private Notifica notifica(String id, String messaggio, LocalDateTime data) {
        return new Notifica(id, NotificaType.LEGACY_MESSAGGIO, Map.of(), messaggio, data);
    }
}
