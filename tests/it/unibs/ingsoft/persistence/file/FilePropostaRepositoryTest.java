package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilePropostaRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void saveFindQueryUpdate_persistonoRoundTripJsonPreservandoOrdine() {
        Path path = tempDir.resolve("proposte.json");
        FilePropostaRepository repository = new FilePropostaRepository(path);
        Proposta aperta = proposta("p1", "Sport", StatoProposta.APERTA);
        Proposta confermata = proposta("p2", "Musica", StatoProposta.CONFERMATA);
        Proposta bozza = proposta("p3", "Teatro", StatoProposta.BOZZA);

        repository.save(aperta);
        repository.save(confermata);
        repository.save(bozza);

        StatoProposta nuovoStato = repository.updateById("p1", proposta -> {
            proposta.confermaSeAperta();
            return proposta.getStato();
        });

        FilePropostaRepository reloaded = new FilePropostaRepository(path);
        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, nuovoStato),
                () -> assertEquals("Sport", reloaded.findById("p1").orElseThrow().getCategoria().getNome()),
                () -> assertEquals(List.of("p1", "p2", "p3"),
                        reloaded.findAll().stream().map(Proposta::getId).toList()),
                () -> assertEquals(List.of("p1", "p2"),
                        reloaded.findByState(StatoProposta.CONFERMATA).stream().map(Proposta::getId).toList())
        );
    }

    private static Proposta proposta(String id, String categoria, StatoProposta stato) {
        return Proposta.rehydrate(
                id,
                List.of(),
                List.of(),
                new Categoria(categoria),
                Map.of("Titolo", id),
                stato,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20),
                List.of(),
                List.of());
    }
}
