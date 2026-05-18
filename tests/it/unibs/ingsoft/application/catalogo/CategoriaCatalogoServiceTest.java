package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaCatalogoServiceTest {
    @Test
    void costruttore_conRepositoryNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CategoriaCatalogoService(null));
    }

    @Test
    void createGetRemoveCategoria_persisteSoloQuandoCambia() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CategoriaCatalogoService service = new CategoriaCatalogoService(repo);

        Categoria sport = service.createCategoria("Sport");
        boolean rimossa = service.removeCategoria("sport");
        boolean assente = service.removeCategoria("sport");

        assertAll(
                () -> assertEquals("Sport", sport.getNome()),
                () -> assertTrue(rimossa),
                () -> assertFalse(assente),
                () -> assertTrue(service.getCategorie().isEmpty())
        );
    }

    @Test
    void rimuoviCategoria_restituisceSuccessoONonTrovato() {
        CategoriaCatalogoService service = new CategoriaCatalogoService(
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        service.createCategoria("Sport");

        assertAll(
                () -> assertEquals(it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult.SUCCESSO,
                        service.rimuoviCategoria("sport")),
                () -> assertEquals(it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult.NON_TROVATO,
                        service.rimuoviCategoria("sport"))
        );
    }

    @Test
    void createCategoria_conDuplicata_lanciaDomainException() {
        CategoriaCatalogoService service = new CategoriaCatalogoService(
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository());
        service.createCategoria("Sport");

        assertThrows(DomainException.class, () -> service.createCategoria("sport"));
    }

    @Test
    void getCategorie_restituisceCategoriePersistite() {
        ApplicationIntegrationSupport.InMemoryCatalogoRepository repo =
                new ApplicationIntegrationSupport.InMemoryCatalogoRepository();
        CategoriaCatalogoService service = new CategoriaCatalogoService(repo);
        Categoria sport = service.createCategoria("Sport");

        assertEquals(List.of(sport), service.getCategorie());
    }
}
