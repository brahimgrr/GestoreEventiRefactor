package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC07_ModificaObbligatorietaCampoComuneTest {
    @Test
    void scenarioPrincipale_campoComuneEsistente_aggiornaObbligatorieta() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());
        graph.configuratoreService().addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));

        assertEquals(CatalogoOperationResult.SUCCESSO,
                graph.configuratoreService().setObbligatorietaCampoComune(
                        new CampoObbligatorietaRequest("Note", true)));
        assertTrue(graph.configuratoreService().getCampiComuni().get(0).isObbligatorio());
    }

    @Test
    void scenarioAlternativo2a_nessunCampoComune_restituisceNonTrovato() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());

        assertEquals(CatalogoOperationResult.NON_TROVATO,
                graph.configuratoreService().setObbligatorietaCampoComune(
                        new CampoObbligatorietaRequest("Note", true)));
    }

    @Test
    void scenarioAlternativoModificaCampo_valoreGiaUguale_restituisceNessunaModifica() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());
        graph.configuratoreService().addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));

        assertEquals(CatalogoOperationResult.NESSUNA_MODIFICA,
                graph.configuratoreService().setObbligatorietaCampoComune(
                        new CampoObbligatorietaRequest("Note", false)));
    }
}
