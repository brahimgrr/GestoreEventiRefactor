package it.unibs.ingsoft.application.batch.dto;

import it.unibs.ingsoft.application.batch.ImportFailure;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImportResultTest {
    @Test
    void incrementiEdErrori_aggiornanoConteggiEStato() {
        ImportResult result = new ImportResult();

        result.incrementCampiComuni();
        result.incrementCategorie();
        result.incrementProposte();
        result.addErrore(new ImportError(new ImportFailure.FileNotFound(Path.of("file"))));

        assertAll(
                () -> assertEquals(1, result.getCampiComuniImportati()),
                () -> assertEquals(1, result.getCategorieImportate()),
                () -> assertEquals(1, result.getProposteImportate()),
                () -> assertEquals(3, result.totaleImportati()),
                () -> assertTrue(result.hasErrors()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> result.getErrori().add(new ImportError(new ImportFailure.FileNotReadable(Path.of("file")))))
        );
    }
}
