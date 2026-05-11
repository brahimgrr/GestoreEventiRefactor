package it.unibs.ingsoft.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
        LocalDateTime data = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertEquals(new Notifica("id-1", "uno", data), new Notifica("id-1", "due", data.plusDays(1)));
    }

    @Test
    void equals_conIdDiverso_restituisceFalse() {
        LocalDateTime data = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertNotEquals(new Notifica("id-1", "uno", data), new Notifica("id-2", "uno", data));
    }

    @Test
    void hashCode_conStessoId_restituisceStessoHashCode() {
        LocalDateTime data = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertEquals(new Notifica("id-1", "uno", data).hashCode(),
                new Notifica("id-1", "due", data.plusDays(1)).hashCode());
    }

    @Test
    void hashCode_conDiversoId_restituisceHashCodeDiverso() {
        LocalDateTime data = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertNotEquals(new Notifica("id-1", "uno", data).hashCode(),
                new Notifica("id-2", "due", data.plusDays(1)).hashCode());
    }
}
