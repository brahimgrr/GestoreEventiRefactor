package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.domain.model.notifica.Notifica;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC17_VisualizzareSpazioPersonaleTest {
    @Test
    void scenarioPrincipale_notifichePresenti_leMostraAlFruitore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Notifica notifica = FunctionalTestSupport.notifica("n-1");
        Fruitore mario = new Fruitore("mario");
        graph.spazioPersonaleRepository().add("mario", notifica);

        assertEquals(notifica, graph.fruitoreService().getNotifiche(mario).get(0));
    }

    @Test
    void scenarioAlternativo2a_spazioPersonaleVuoto_restituisceListaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Fruitore mario = new Fruitore("mario");

        assertTrue(graph.fruitoreService().getNotifiche(mario).isEmpty());
    }

    @Test
    void scenarioAlternativo4a_eliminazioneNotifica_estendeUC21() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Fruitore mario = new Fruitore("mario");
        Notifica notifica = FunctionalTestSupport.notifica("n-1");
        graph.spazioPersonaleRepository().add("mario", notifica);

        graph.fruitoreService().cancellaNotifica(mario, notifica);

        assertTrue(graph.fruitoreService().getNotifiche(mario).isEmpty());
    }
}
