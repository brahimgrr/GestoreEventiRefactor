package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC14_PubblicarePropostaBacheca_Test {
    @Test
    void scenarioPrincipale_propostaValidaSalvata_pubblicaInBacheca() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaValida(graph, "Pubblicabile", "2");
        graph.configuratoreService().salvaProposta(proposta);

        graph.configuratoreService().pubblicaProposta(proposta);

        assertAll(
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(List.of(proposta), graph.configuratoreService().getBachecaPerCategoria().get("Sport")),
                () -> assertTrue(graph.configuratoreService().getProposteValide().isEmpty())
        );
    }

    @Test
    void scenarioAlternativo2a_nessunaPropostaValida_listaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.configuratoreService().getProposteValide().isEmpty());
    }

    /*
    TEST INUTILE??
     */
    @Test
    void scenarioAlternativo3a_configuratoreAnnulla_nonPubblica() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaValida(graph, "Annullata prima della pubblicazione", "2");
        graph.configuratoreService().salvaProposta(proposta);

        assertTrue(graph.configuratoreService().getBachecaPerCategoria().isEmpty());
    }

    @Test
    void scenarioAlternativo8a_propostaDuplicataInBacheca_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Proposta prima = FunctionalTestSupport.propostaValida(graph, "Duplicata", "2");
        Proposta seconda = FunctionalTestSupport.propostaValida(graph, "Duplicata", "2");
        graph.configuratoreService().salvaProposta(prima);
        graph.configuratoreService().pubblicaProposta(prima);

        assertThrows(IllegalStateException.class, () -> graph.configuratoreService().salvaProposta(seconda));
    }

    @Test
    void scenarioAlternativo7a_termineIscrizioneScaduto_nonPubblica() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPersistita(
                StatoProposta.VALIDA,
                "Scaduta",
                "2",
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(2),
                List.of());

        assertThrows(IllegalStateException.class, () -> graph.configuratoreService().pubblicaProposta(proposta));
    }
}
