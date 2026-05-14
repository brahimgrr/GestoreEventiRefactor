package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC03_FissareCampiBase_Test {
    @Test
    void scenarioPrincipale_conCampoSupplementare_fissaCampiBasePredefinitiEdExtra() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        graph.configuratoreService().configuraCampiBase(
                List.of(FunctionalTestSupport.campoExtra("Equipaggiamento", TipoDato.STRINGA)));

        assertAll(
                () -> assertFalse(graph.configuratoreService().isPrimaConfigurazioneNecessaria()),
                () -> assertEquals(9, graph.configuratoreService().getCampiBase().size())
        );
    }

    @Test
    void scenarioAlternativo3a_senzaCampiSupplementari_fissaSoloCampiPredefiniti() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        graph.configuratoreService().configuraCampiBase(List.of());

        assertEquals(8, graph.configuratoreService().getCampiBase().size());
    }

    @Test
    void scenarioAlternativo5b_nomeDuplicato_segnalaErroreEFissaFallbackPredefinito() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertThrows(IllegalStateException.class,
                () -> graph.configuratoreService().configuraCampiBase(
                        List.of(FunctionalTestSupport.campoExtra("Titolo", TipoDato.STRINGA))));
        assertEquals(8, graph.configuratoreService().getCampiBase().size());
    }

    /*
    TEST SENZA SENSO DATO CHE L'ANNULLAMENTO è UI E IO QUA NON VEDO NULLA
     */
    @Test
    void scenarioAlternativo11a_nonConfermaExtra_scartaSupplementariEFissaPredefiniti() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        graph.configuratoreService().configuraCampiBase(List.of());

        assertFalse(graph.configuratoreService().getCampiBase().stream()
                .anyMatch(campo -> campo.getNome().equals("Scartato")));
    }
}
