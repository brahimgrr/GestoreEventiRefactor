package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.persistence.file.FileCatalogoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UC03_UC12_CatalogoManagementIT {
    @TempDir
    Path tempDir;

    @Test
    void UC03_UC12_catalogoOperationsPersistThroughFileRepository() {
        Path catalogoFile = tempDir.resolve("catalogo.json");
        CatalogoService service = new CatalogoService(new FileCatalogoRepository(catalogoFile));

        service.initiateCampiBase();
        service.addCampoComune("Difficolta", TipoDato.STRINGA, false);
        service.setObbligatorietaCampoComune("Difficolta", true);
        service.createCategoria("Escursione");
        service.addCampoSpecifico("Escursione", "Guida", TipoDato.STRINGA, false);
        service.setObbligatorietaCampoSpecifico("Escursione", "Guida", true);

        CatalogoService reloaded = new CatalogoService(new FileCatalogoRepository(catalogoFile));

        assertEquals(8, reloaded.getCampiBase().size());
        assertEquals(1, reloaded.getCampiComuni().size());
        assertTrue(reloaded.getCampiComuni().get(0).isObbligatorio());
        assertEquals(1, reloaded.getCategorie().size());
        assertEquals("Escursione", reloaded.getCategorie().get(0).getNome());
        assertEquals(1, reloaded.getCategorie().get(0).getCampiSpecifici().size());
        assertTrue(reloaded.getCategorie().get(0).getCampiSpecifici().get(0).isObbligatorio());
    }
}
