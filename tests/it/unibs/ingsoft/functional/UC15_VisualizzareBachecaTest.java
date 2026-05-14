package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.utente.Fruitore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC15_VisualizzareBachecaTest {
    @Test
    void scenarioPrincipale_proposteApertePresenti_mostraBachecaPerCategoria() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "In bacheca", "2");

        assertEquals(proposta, graph.fruitoreService().getBachecaPerCategoria().get("Sport").get(0));
    }

    @Test
    void scenarioAlternativo2a_bachecaVuota_restituisceMappaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.fruitoreService().getBachecaPerCategoria().isEmpty());
    }

    @Test
    void scenarioAlternativo5a_fruitoreConNotifiche_puoVedereNotificheCollegateAllaBacheca() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Notifica notifica = new Notifica("messaggio");
        graph.spazioPersonaleRepository().load().getSpazioDi("mario").addNotifica(notifica);

        Fruitore mario = new Fruitore("mario");

        assertEquals(1, graph.fruitoreService().getNotifiche(mario).size());
    }
}
