package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC19_VisualizzareArchivioProposteTest {
    @Test
    void scenarioPrincipale_propostePresenti_leRaggruppaPerStato() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "Archivio aperta", "2");

        assertEquals(proposta, graph.configuratoreService().getPropostePerStato().get(StatoProposta.APERTA).get(0));
    }

    @Test
    void scenarioAlternativo3a_archivioVuoto_restituisceMappaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.configuratoreService().getPropostePerStato().isEmpty());
    }

    @Test
    void scenarioAlternativo_nessunaPropostaPerStatoRichiesto_statoAssente() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        FunctionalTestSupport.propostaPubblicata(graph, "Solo aperta", "2");

        assertFalse(graph.configuratoreService().getPropostePerStato().containsKey(StatoProposta.CONCLUSA));
    }
}
