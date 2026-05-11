package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.Catalogo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class FileRepositoryContractTest {
    @TempDir
    Path tempDir;

    @Test
    void loadReturnsEmptyAggregateWhenFileDoesNotExist() {
        ICatalogoRepository repository = new FileCatalogoRepository(tempDir.resolve("catalogo.json"));

        Catalogo catalogo = repository.load();

        assertNotNull(catalogo);
        assertTrue(catalogo.getCategorie().isEmpty());
    }

    @Test
    void savePersistsTheAggregatePassedAsParameter() {
        Path path = tempDir.resolve("catalogo.json");
        ICatalogoRepository writer = new FileCatalogoRepository(path);
        Catalogo catalogo = new Catalogo();
        catalogo.addCategoria("Cinema");

        writer.save(catalogo);

        ICatalogoRepository reader = new FileCatalogoRepository(path);
        assertEquals(
                "Cinema",
                reader.load().getCategorie().stream()
                        .map(Categoria::getNome)
                        .findFirst()
                        .orElseThrow()
        );
    }

    @Test
    void loadThrowsWhenJsonIsCorrupt() throws Exception {
        Path path = tempDir.resolve("catalogo.json");
        Files.writeString(path, "{ not valid json");
        ICatalogoRepository repository = new FileCatalogoRepository(path);

        assertThrows(UncheckedIOException.class, repository::load);
    }

    @Test
    void saveFallsBackToReplaceMoveWhenAtomicMoveIsNotSupported() {
        Path path = tempDir.resolve("fallback.json");
        FallbackMoveRepository repository = new FallbackMoveRepository(path);

        repository.save(new StoredName("Cinema"));

        StoredName saved = repository.load();
        assertEquals("Cinema", saved.name());
        assertTrue(repository.usedReplaceMove());
    }

    private static final class FallbackMoveRepository extends AbstractFileRepository<StoredName> {
        private boolean usedReplaceMove;

        private FallbackMoveRepository(Path path) {
            super(path, StoredName.class, () -> new StoredName(""));
        }

        @Override
        protected void moveAtomically(Path tmp, Path target) throws java.io.IOException {
            throw new AtomicMoveNotSupportedException(tmp.toString(), target.toString(), "test fallback");
        }

        @Override
        protected void moveReplacing(Path tmp, Path target) throws java.io.IOException {
            usedReplaceMove = true;
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }

        boolean usedReplaceMove() {
            return usedReplaceMove;
        }
    }

    private record StoredName(String name) {
    }
}
