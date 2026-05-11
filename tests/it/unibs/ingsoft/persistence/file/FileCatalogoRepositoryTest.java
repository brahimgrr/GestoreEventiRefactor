package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.catalogo.Catalogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileCatalogoRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void get_conFileAssente_restituisceCatalogoVuoto() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        Catalogo catalogo = repository.get();

        assertTrue(catalogo.getCategorie().isEmpty());
    }

    @Test
    void get_quandoInvocatoDueVolte_restituisceStessaIstanzaCached() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        assertSame(repository.get(), repository.get());
    }

    @Test
    void save_senzaGetPrecedente_nonCreaFile() {
        Path path = tempDir.resolve("catalogo.json");
        FileCatalogoRepository repository = new FileCatalogoRepository(path);

        repository.save();

        assertFalse(Files.exists(path));
    }

    @Test
    void save_dopoModificaDelCatalogoCached_persistelaCategoria() {
        Path path = tempDir.resolve("catalogo.json");
        FileCatalogoRepository repository = new FileCatalogoRepository(path);
        repository.get().addCategoria("Sport");

        repository.save();
        Catalogo ricaricato = new FileCatalogoRepository(path).get();

        assertEquals("Sport", ricaricato.getCategorie().get(0).getNome());
    }
}
