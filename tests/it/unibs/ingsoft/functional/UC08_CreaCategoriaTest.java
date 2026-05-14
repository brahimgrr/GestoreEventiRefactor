package it.unibs.ingsoft.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC08_CreaCategoriaTest {
    @Test
    void scenarioPrincipale_nomeValido_creaCategoriaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        graph.configuratoreService().createCategoria("Sport");

        assertAll(
                () -> assertEquals("Sport", graph.configuratoreService().getCategorie().get(0).getNome()),
                () -> assertTrue(graph.configuratoreService().getCategorie().get(0).getCampiSpecifici().isEmpty())
        );
    }

    /*
    TEST NON SENSATO: stesso problema di annullamento ui ma al programma non arriva nulla
     */
    @Test
    void scenarioAlternativo3a_configuratoreAnnulla_nonCreaCategoria() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.configuratoreService().getCategorie().isEmpty());
    }

    @Test
    void scenarioAlternativo4a_nomeDuplicato_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().createCategoria("Sport");

        assertThrows(IllegalStateException.class, () -> graph.configuratoreService().createCategoria("sport"));
    }
}
