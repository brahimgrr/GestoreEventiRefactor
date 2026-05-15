package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.notifica.NotificaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNotificationRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void addFindDelete_persistonoRoundTripJsonPerUtenteNormalizzato() {
        Path path = tempDir.resolve("notifiche.json");
        FileNotificationRepository repository = new FileNotificationRepository(path);
        Notifica confermata = new Notifica(
                "n1",
                NotificaType.PROPOSTA_CONFERMATA,
                Map.of("propostaId", "p1"),
                null,
                LocalDateTime.of(2026, 1, 1, 9, 0));
        Notifica legacy = new Notifica("n2", "Messaggio", LocalDateTime.of(2026, 1, 2, 10, 0));

        repository.add(" Mario ", confermata);
        repository.add("mario", legacy);

        boolean deleted = repository.delete("MARIO", "n1");
        boolean missing = repository.delete("mario", "missing");
        List<Notifica> reloaded = new FileNotificationRepository(path).findByUsername(" mario ");

        assertAll(
                () -> assertTrue(deleted),
                () -> assertFalse(missing),
                () -> assertEquals(List.of("n2"), reloaded.stream().map(Notifica::id).toList()),
                () -> assertEquals("Messaggio", reloaded.get(0).messaggio())
        );
    }
}
