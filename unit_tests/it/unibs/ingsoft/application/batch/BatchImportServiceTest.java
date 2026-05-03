package it.unibs.ingsoft.application.batch;

import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import it.unibs.ingsoft.testsupport.InMemoryBachecaRepository;
import it.unibs.ingsoft.testsupport.InMemoryCatalogoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BatchImportServiceTest {
    @TempDir
    Path tempDir;

    private CatalogoService catalogoService;
    private PropostaService propostaService;
    private BatchImportService service;

    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
        catalogoService = new CatalogoService(new InMemoryCatalogoRepository());
        catalogoService.initiateCampiBase();
        propostaService = new PropostaService(new InMemoryBachecaRepository());
        service = new BatchImportService(catalogoService, propostaService);
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC22_importaValidData_importsCommonCategoryAndValidProposal() throws IOException {
        Path file = writeJson("valid.json", DomainFixtures.validImportJson());

        ImportResult result = service.importa(file);

        assertFalse(result.hasErrors());
        assertEquals(1, result.getCampiComuniImportati());
        assertEquals(1, result.getCategorieImportate());
        assertEquals(1, result.getProposteImportate());
        assertEquals(1, propostaService.getProposteValide().size());
    }

    @Test
    void UC22_importaBestEffort_skipsInvalidElementsAndContinues() throws IOException {
        Path file = writeJson("best-effort.json", """
                {
                  "campiComuni": [
                    {"nome":"Difficolta","tipoDato":"TIPO_ERRATO","obbligatorio":false}
                  ],
                  "categorie": [
                    {"nome":"Escursione","campiSpecifici":[]}
                  ],
                  "proposte": [
                    {"categoria":"Escursione","valoriCampi":{
                      "Titolo":"Giro sul lago",
                      "Numero di partecipanti":"3",
                      "Termine ultimo di iscrizione":"15/01/2026",
                      "Luogo":"Brescia",
                      "Data":"18/01/2026",
                      "Ora":"16:30",
                      "Quota individuale":"12.50",
                      "Data conclusiva":"18/01/2026"
                    }}
                  ]
                }
                """);

        ImportResult result = service.importa(file);

        assertTrue(result.hasErrors());
        assertEquals(0, result.getCampiComuniImportati());
        assertEquals(1, result.getCategorieImportate());
        assertEquals(1, result.getProposteImportate());
        assertEquals(1, propostaService.getProposteValide().size());
    }

    @Test
    void UC22_importaMissingFile_throwsIOException() {
        Path missing = tempDir.resolve("missing.json");

        assertThrows(IOException.class, () -> service.importa(missing));
    }

    @Test
    void UC22_importaEmptyFile_noImportedElementsAndNoErrors() throws IOException {
        Path file = writeJson("empty.json", "{}");

        ImportResult result = service.importa(file);

        assertFalse(result.hasErrors());
        assertEquals(0, result.totaleImportati());
    }

    private Path writeJson(String fileName, String json) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, json);
        return file;
    }
}
