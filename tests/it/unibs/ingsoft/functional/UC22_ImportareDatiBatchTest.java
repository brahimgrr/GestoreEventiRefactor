package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.batch.ImportFailure;
import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.error.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC22_ImportareDatiBatchTest {
    @TempDir
    Path tempDir;

    @Test
    void scenarioPrincipale_fileJsonValido_importaElementiEProposteValide() throws Exception {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());
        Path file = FunctionalTestSupport.writeBatchJson(tempDir, FunctionalTestSupport.batchJsonValido());

        ImportResult result = graph.configuratoreService().importa(file);

        assertAll(
                () -> assertEquals(1, result.getCampiComuniImportati()),
                () -> assertEquals(1, result.getCategorieImportate()),
                () -> assertEquals(1, result.getProposteImportate()),
                () -> assertEquals(1, graph.configuratoreService().getProposteValide().size())
        );
    }

    @Test
    void scenarioAlternativo4a_fileAssente_segnalaErroreEAnnullaImportazione() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> graph.configuratoreService().importa(tempDir.resolve("assente.json")));

        assertInstanceOf(ImportFailure.FileNotFound.class, exception.failure());
    }

    @Test
    void scenarioAlternativo5a_elementoNonValido_vieneScartatoEImportContinua() throws Exception {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());
        Path file = FunctionalTestSupport.writeBatchJson(tempDir, """
                {
                  "campiComuni": [
                    { "nome": "Note", "tipoDato": "STRINGA", "obbligatorio": false },
                    { "nome": "Eta", "tipoDato": "NON_VALIDO", "obbligatorio": false }
                  ],
                  "categorie": [],
                  "proposte": []
                }
                """);

        ImportResult result = graph.configuratoreService().importa(file);

        assertAll(
                () -> assertEquals(1, result.getCampiComuniImportati()),
                () -> assertInstanceOf(ImportFailure.CommonFieldTypeInvalid.class,
                        result.getErrori().get(0).failure())
        );
    }

    @Test
    void scenarioAlternativo5b_fileSenzaDatiImportabili_restituisceZeroImportati() throws Exception {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Path file = FunctionalTestSupport.writeBatchJson(tempDir, """
                { "campiComuni": [], "categorie": [], "proposte": [] }
                """);

        ImportResult result = graph.configuratoreService().importa(file);

        assertEquals(0, result.totaleImportati());
    }
}
