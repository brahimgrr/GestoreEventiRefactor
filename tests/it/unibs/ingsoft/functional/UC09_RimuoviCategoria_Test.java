package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC09_RimuoviCategoria_Test {
    @Test
    void scenarioPrincipale_categoriaEsistente_rimuoveCategoriaECampiSpecifici() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");
        graph.configuratoreService().addCampoSpecifico("Sport",
                new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertEquals(CatalogoOperationResult.SUCCESSO, graph.configuratoreService().rimuoviCategoria("Sport"));
        assertTrue(graph.configuratoreService().getCategorie().isEmpty());
    }

    @Test
    void scenarioAlternativo2a_nessunaCategoria_restituisceNonTrovato() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertEquals(CatalogoOperationResult.NON_TROVATO, graph.configuratoreService().rimuoviCategoria("Sport"));
    }

    /*
    TEST INUTILE
     */
    @Test
    void scenarioAlternativo5a_nonConfermaRimozione_nonModificaCatalogo() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");

        assertEquals(1, graph.configuratoreService().getCategorie().size());
    }
}
