package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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

    private Notifica notifica(String id, String messaggio, LocalDateTime data) {
        return new Notifica(id, NotificaType.LEGACY_MESSAGGIO, Map.of(), messaggio, data);
    }
}
