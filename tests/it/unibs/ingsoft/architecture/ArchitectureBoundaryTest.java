package it.unibs.ingsoft.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureBoundaryTest {
    private static final Path SRC = Path.of("src", "it", "unibs", "ingsoft");

    @Test
    void domain_nonDipendeDaJacksonOPersistence() throws IOException {
        List<Path> offenders = javaFiles(SRC.resolve("domain")).stream()
                .filter(path -> contains(path, "com.fasterxml.jackson") || contains(path, "it.unibs.ingsoft.persistence"))
                .toList();

        assertEquals(List.of(), offenders);
    }

    @Test
    void application_nonDipendeDaDocumentOPersistenceDto() throws IOException {
        List<Path> offenders = javaFiles(SRC.resolve("application")).stream()
                .filter(path -> contains(path, "it.unibs.ingsoft.persistence.dto")
                        || contains(path, "it.unibs.ingsoft.persistence.file.document"))
                .toList();

        assertEquals(List.of(), offenders);
    }

    @Test
    void persistenceDtoPackage_nonEsponePiuTipiPubblici() throws IOException {
        Path dtoDir = SRC.resolve(Path.of("persistence", "dto"));

        if (!Files.exists(dtoDir)) {
            assertTrue(true);
            return;
        }

        assertEquals(List.of(), javaFiles(dtoDir));
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return List.of();
        }
        try (var paths = Files.walk(root)) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(Path::normalize)
                    .toList();
        }
    }

    private static boolean contains(Path path, String needle) {
        try {
            return Files.readString(path).contains(needle);
        } catch (IOException e) {
            throw new AssertionError("Cannot read " + path, e);
        }
    }
}
