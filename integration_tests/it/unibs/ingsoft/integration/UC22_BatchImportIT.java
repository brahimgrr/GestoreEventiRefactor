package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.batch.ImportResult;
import it.unibs.ingsoft.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UC22_BatchImportIT {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC22_fileBackedBatchImportAddsValidDataAndLeavesProposalPublishable() throws IOException {
        Path catalogoFile = tempDir.resolve("catalogo.json");
        Path proposteFile = tempDir.resolve("proposte.json");
        CatalogoService catalogoService = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        catalogoService.initiateCampiBase();
        PropostaService propostaService = new PropostaService(new FileBachecaRepository(proposteFile));
        BatchImportService importService = new BatchImportService(catalogoService, propostaService);
        Path importFile = tempDir.resolve("import.json");
        Files.writeString(importFile, DomainFixtures.validImportJson());

        ImportResult result = importService.importa(importFile);

        assertEquals(3, result.totaleImportati());
        assertFalse(result.hasErrors());
        assertEquals(1, propostaService.getProposteValide().size());

        CatalogoService reloadedCatalogo = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        assertEquals(1, reloadedCatalogo.getCampiComuni().size());
        assertEquals(1, reloadedCatalogo.getCategorie().size());
    }
}
