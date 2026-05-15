package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC11_RimuoviCampoSpecificoTest {
    @Test
    void scenarioPrincipale_campoSpecificoEsistente_loRimuove() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");
        graph.configuratoreService().addCampoSpecifico("Sport",
                new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertEquals(CatalogoOperationResult.SUCCESSO,
                graph.configuratoreService().rimuoviCampoSpecifico("Sport", "Arbitro"));
        assertTrue(graph.configuratoreService().getCategorie().get(0).getCampiSpecifici().isEmpty());
    }

    @Test
    void scenarioAlternativo2a_nessunaCategoria_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.configuratoreService().rimuoviCampoSpecifico("Sport", "Arbitro"));

        assertInstanceOf(CatalogFailure.CategoryNotFound.class, exception.failure());
    }

    @Test
    void scenarioAlternativo6a_categoriaSenzaCampiSpecifici_restituisceNonTrovato() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");

        assertEquals(CatalogoOperationResult.NON_TROVATO,
                graph.configuratoreService().rimuoviCampoSpecifico("Sport", "Arbitro"));
    }
}
