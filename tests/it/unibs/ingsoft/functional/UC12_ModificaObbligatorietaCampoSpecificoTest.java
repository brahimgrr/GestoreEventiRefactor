package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.TipoDato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC12_ModificaObbligatorietaCampoSpecificoTest {
    @Test
    void scenarioPrincipale_campoSpecificoEsistente_aggiornaObbligatorieta() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");
        graph.configuratoreService().addCampoSpecifico("Sport",
                new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertEquals(CatalogoOperationResult.SUCCESSO,
                graph.configuratoreService().setObbligatorietaCampoSpecifico(
                        "Sport", new CampoObbligatorietaRequest("Arbitro", true)));
        assertTrue(graph.configuratoreService().getCategorie().get(0).getCampiSpecifici().get(0).isObbligatorio());
    }

    @Test
    void scenarioAlternativo2a_nessunaCategoria_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertThrows(IllegalStateException.class,
                () -> graph.configuratoreService().setObbligatorietaCampoSpecifico(
                        "Sport", new CampoObbligatorietaRequest("Arbitro", true)));
    }

    @Test
    void scenarioAlternativo_categoriaSenzaCampiSpecifici_restituisceNonTrovato() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");

        assertEquals(CatalogoOperationResult.NON_TROVATO,
                graph.configuratoreService().setObbligatorietaCampoSpecifico(
                        "Sport", new CampoObbligatorietaRequest("Arbitro", true)));
    }
}
