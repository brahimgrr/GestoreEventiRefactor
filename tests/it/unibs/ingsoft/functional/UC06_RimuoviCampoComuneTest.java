package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC06_RimuoviCampoComuneTest {
    @Test
    void scenarioPrincipale_campoComuneEsistente_loRimuoveDalCatalogo() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());
        graph.configuratoreService().addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));

        assertEquals(CatalogoOperationResult.SUCCESSO, graph.configuratoreService().rimuoviCampoComune("note"));
        assertTrue(graph.configuratoreService().getCampiComuni().isEmpty());
    }

    @Test
    void scenarioAlternativo2a_nessunCampoComune_restituisceNonTrovato() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());

        assertEquals(CatalogoOperationResult.NON_TROVATO,
                graph.configuratoreService().rimuoviCampoComune("Note"));
    }
}
