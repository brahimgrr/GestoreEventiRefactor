package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBachecaRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void load_conFileAssente_restituisceBachecaVuota() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        BachecaDTO bacheca = repository.load();

        assertTrue(bacheca.getProposte().isEmpty());
    }

    @Test
    void load_quandoInvocatoDueVolte_restituisceIstanzeDistinte() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        assertNotSame(repository.load(), repository.load());
    }

    @Test
    void save_conBachecaVuota_creaFile() throws Exception {
        Path path = tempDir.resolve("bacheca.json");

        FileBachecaRepository repository = new FileBachecaRepository(path);
        repository.save(new BachecaDTO());

        assertTrue(Files.exists(path));
    }

    @Test
    void save_conBachecaModificata_persisteLaProposta() {
        Path path = tempDir.resolve("bacheca.json");

        FileBachecaRepository repository = new FileBachecaRepository(path);
        BachecaDTO bacheca = repository.load();
        bacheca.addProposta(new Proposta(new Categoria("Sport"), List.of(), List.of()));
        repository.save(bacheca);

        BachecaDTO ricaricata = new FileBachecaRepository(path).load();

        assertEquals(1, ricaricata.getProposte().size());
    }
}
