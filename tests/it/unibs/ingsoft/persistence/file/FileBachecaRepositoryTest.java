package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.proposta.Bacheca;
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
    void get_conFileAssente_restituisceBachecaVuota() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        Bacheca bacheca = repository.get();

        assertTrue(bacheca.getProposte().isEmpty());
    }

    @Test
    void get_quandoInvocatoDueVolte_restituisceStessaIstanzaCached() {
        FileBachecaRepository repository = new FileBachecaRepository(tempDir.resolve("bacheca.json"));

        assertSame(repository.get(), repository.get());
    }

    @Test
    void save_senzaGetPrecedente_nonCreaFile() {
        Path path = tempDir.resolve("bacheca.json");
        FileBachecaRepository repository = new FileBachecaRepository(path);

        repository.save();

        assertFalse(Files.exists(path));
    }

    @Test
    void save_dopoModificaDellaBachecaCached_persistelaProposta() {
        Path path = tempDir.resolve("bacheca.json");
        FileBachecaRepository repository = new FileBachecaRepository(path);
        repository.get().addProposta(new Proposta(new Categoria("Sport"), List.of(), List.of()));

        repository.save();
        Bacheca ricaricata = new FileBachecaRepository(path).get();

        assertEquals(1, ricaricata.getProposte().size());
    }
}
