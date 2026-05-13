package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.catalogo.Catalogo;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRepositoryFactoryTest {
    @TempDir
    Path tempDir;

    @Test
    void customBasePathControlsRepositoryDataLocation() {
        FileRepositoryFactory factory = new FileRepositoryFactory(tempDir);
        ICatalogoRepository catalogoRepository = factory.createCatalogoRepository();

        catalogoRepository.save(new Catalogo());

        assertTrue(Files.exists(tempDir.resolve("catalogo.json")));
    }
}
