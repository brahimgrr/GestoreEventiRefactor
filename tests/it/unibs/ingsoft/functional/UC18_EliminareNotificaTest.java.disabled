package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.notifica.Notifica;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC18_EliminareNotificaTest {
    @Test
    void scenarioPrincipale_notificaPresente_laEliminaDalloSpazioPersonale() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Fruitore mario = new Fruitore("mario");
        Notifica notifica = FunctionalTestSupport.notifica("n-1");
        graph.spazioPersonaleRepository().load().getSpazioDi("mario").addNotifica(notifica);

        graph.fruitoreService().cancellaNotifica(mario, notifica);

        assertTrue(graph.fruitoreService().getNotifiche(mario).isEmpty());
    }

    @Test
    void scenarioAlternativo_nessunaNotificaDisponibile_nonSalvaModifiche() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Fruitore mario = new Fruitore("mario");
        graph.fruitoreService().cancellaNotifica(mario, FunctionalTestSupport.notifica("assente"));

        assertEquals(0, graph.spazioPersonaleRepository().saveCount());
    }

    /*
    TEST INUTILE: visibile in ui ma qua non si vede differenza
     */
    @Test
    void scenarioAlternativo3a_fruitoreAnnulla_notificaRimanePresente() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Notifica notifica = FunctionalTestSupport.notifica("n-1");
        graph.spazioPersonaleRepository().load().getSpazioDi("mario").addNotifica(notifica);

        assertEquals(1, graph.fruitoreService().getNotifiche(new Fruitore("mario")).size());
    }
}
