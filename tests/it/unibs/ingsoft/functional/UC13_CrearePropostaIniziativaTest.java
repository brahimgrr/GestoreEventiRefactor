package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UC13_CrearePropostaIniziativaTest {
    @Test
    void scenarioPrincipale_valoriValidi_creaValidaESalvaProposta() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaValida(graph, "Torneo", "2");

        graph.configuratoreService().salvaProposta(proposta);

        assertAll(
                () -> assertEquals(StatoProposta.VALIDA, proposta.getStato()),
                () -> assertEquals(1, graph.configuratoreService().getProposteValide().size())
        );
    }

    @Test
    void scenarioAlternativo2a_nessunaCategoriaDisponibile_nonCreaProposta() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.configuratoreService().getCategorie().isEmpty());
    }

    @Test
    void scenarioAlternativo5a_campoObbligatorioMancante_restituisceErrori() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaValida(graph, "Completa", "2");
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, ""));

        PropostaValidationResult result = graph.configuratoreService().applicaValoriEValida(
                proposta, proposta.getValoriCampi());

        assertFalse(result.valida());
    }

    @Test
    void scenarioAlternativo5b_numeroPartecipantiNonValido_restituisceErrori() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaValida(graph, "Partecipanti invalidi", "2");

        PropostaValidationResult result = graph.configuratoreService().applicaValoriEValida(
                proposta, FunctionalTestSupport.valoriProposta("Partecipanti invalidi", "molti"));

        assertFalse(result.valida());
    }
}
