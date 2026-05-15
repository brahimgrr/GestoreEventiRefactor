package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC16_IscriversiPropostaApertaTest {
    @Test
    void scenarioPrincipale_propostaApertaConPostiDisponibili_iscriveFruitore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "Con posti", "2");

        Fruitore mario = new Fruitore("mario");

        graph.fruitoreService().iscrivi(proposta, mario);

        assertTrue(proposta.isIscritto("mario"));
    }

    @Test
    void scenarioAlternativo_nessunaPropostaAperta_bachecaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.fruitoreService().getBachecaPerCategoria().isEmpty());
    }

    @Test
    void scenarioAlternativo2a_fruitoreGiaIscritto_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "Gia iscritto", "2");
        Fruitore mario = new Fruitore("mario");
        graph.fruitoreService().iscrivi(proposta, mario);

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.fruitoreService().iscrivi(proposta, mario));

        assertInstanceOf(ProposalFailure.AlreadySubscribed.class, exception.failure());
    }

    @Test
    void scenario_postiEsauriti_confermaPropostaENonConsenteNuovaIscrizione() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "Pieno", "1");

        Fruitore mario = new Fruitore("mario");
        graph.fruitoreService().iscrivi(proposta, mario);

        Fruitore luigi = new Fruitore("luigi");

        assertAll(
                () -> assertEquals(StatoProposta.CONFERMATA, proposta.getStato()),
                () -> {
                    DomainException exception = assertThrows(DomainException.class,
                            () -> graph.fruitoreService().iscrivi(proposta, luigi));
                    assertInstanceOf(ProposalFailure.NotOpenForSubscription.class, exception.failure());
                }
        );
    }
}
