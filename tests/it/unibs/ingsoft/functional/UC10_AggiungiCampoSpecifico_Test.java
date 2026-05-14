package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.domain.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC10_AggiungiCampoSpecifico_Test {
    @Test
    void scenarioPrincipale_categoriaEsistente_aggiungeCampoSpecifico() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");

        graph.configuratoreService().addCampoSpecifico("Sport",
                new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertEquals("Arbitro", graph.configuratoreService().getCategorie().get(0).getCampiSpecifici().get(0).getNome());
    }

    @Test
    void scenarioAlternativo2a_nessunaCategoria_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.configuratoreService().addCampoSpecifico("Sport",
                        new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false)));

        assertInstanceOf(CatalogFailure.CategoryNotFound.class, exception.failure());
    }

    /*
    TEST NON SENSATO
     */
    @Test
    void scenarioAlternativo3a_configuratoreAnnulla_nonAggiungeCampiSpecifici() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");

        assertTrue(graph.configuratoreService().getCategorie().get(0).getCampiSpecifici().isEmpty());
    }
}
