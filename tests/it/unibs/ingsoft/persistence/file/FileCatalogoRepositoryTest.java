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
    void load_conFileAssente_restituisceCatalogoVuoto() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        Catalogo catalogo = repository.load();

        assertTrue(catalogo.getCategorie().isEmpty());
    }

    @Test
    void load_quandoInvocatoDueVolte_restituisceIstanzeDistinte() {
        FileCatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        assertNotSame(repository.load(), repository.load());
    }

    @Test
    void save_conCatalogoVuoto_creaFile() {
        Path path = tempDir.resolve("catalogo.json");

        FileCatalogoRepository repository = new FileCatalogoRepository(path);
        repository.save(new Catalogo());

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conCatalogoModificato_persisteLaCategoria() {
        Path path = tempDir.resolve("catalogo.json");

        FileCatalogoRepository repository = new FileCatalogoRepository(path);
        Catalogo catalogo = repository.load();
        catalogo.addCategoria("Sport");
        repository.save(catalogo);

        Catalogo ricaricato = new FileCatalogoRepository(path).load();

        assertEquals("Sport", ricaricato.getCategorie().get(0).getNome());
    }
}
