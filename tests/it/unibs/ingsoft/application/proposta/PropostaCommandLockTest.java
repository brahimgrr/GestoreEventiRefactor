package it.unibs.ingsoft.application.proposta;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PropostaCommandLockTest {
    @Test
    void runLocked_esegueAzioneESbloccaAncheConEccezione() {
        PropostaCommandLock lock = new PropostaCommandLock();
        AtomicInteger count = new AtomicInteger();

        lock.runLocked(count::incrementAndGet);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> lock.runLocked(() -> {
                    throw new RuntimeException("boom");
                }));
        lock.runLocked(count::incrementAndGet);

        assertAll(
                () -> assertEquals("boom", exception.getMessage()),
                () -> assertEquals(2, count.get())
        );
    }

    @Test
    void callLocked_restituisceValoreESbloccaAncheConEccezione() {
        PropostaCommandLock lock = new PropostaCommandLock();

        String value = lock.callLocked(() -> "ok");
        assertThrows(RuntimeException.class, () -> lock.callLocked(() -> {
            throw new RuntimeException("boom");
        }));

        assertEquals("ok", value);
    }

    @Test
    void metodi_conActionNull_lancianoNullPointerException() {
        PropostaCommandLock lock = new PropostaCommandLock();

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> lock.runLocked(null)),
                () -> assertThrows(NullPointerException.class, () -> lock.callLocked(null))
        );
    }
}
