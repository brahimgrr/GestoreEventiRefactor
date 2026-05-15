package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.error.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC21_DisdireIscrizione_Test {
    @Test
    void scenarioPrincipale_iscrizioneAttiva_rimuoveFruitoreDagliAderenti() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Fruitore mario = new Fruitore("mario");
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "Disdicibile", "2");
        graph.fruitoreService().iscrivi(proposta, mario);

        graph.fruitoreService().disiscrivi(proposta, mario);

        assertFalse(proposta.isIscritto("mario"));
    }

    @Test
    void scenarioAlternativo2a_nessunaIscrizioneAttiva_listaVuota() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        Fruitore mario = new Fruitore("mario");

        assertTrue(graph.fruitoreService().getProposteAperteIscritteDa(mario).isEmpty());
    }

    /*
    TEST INUTILE: annullamento si vede in ui ma non si vede nel programma
     */
    @Test
    void scenarioAlternativo5a_nonConfermaDisdetta_iscrizioneRimane() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Fruitore mario = new Fruitore("mario");
        Proposta proposta = FunctionalTestSupport.propostaPubblicata(graph, "Non disdetta", "2");
        graph.fruitoreService().iscrivi(proposta, mario);

        assertTrue(proposta.isIscritto("mario"));
    }

    @Test
    void scenarioAlternativo6a_termineIscrizioneScaduto_nonDisiscrive() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        Proposta proposta = FunctionalTestSupport.propostaPersistita(
                StatoProposta.APERTA,
                "Scaduta iscrizione",
                "2",
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(3),
                List.of("mario"));
        graph.bachecaRepository().save(proposta);

        Fruitore mario = new Fruitore("mario");

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.fruitoreService().disiscrivi(proposta, mario));

        assertInstanceOf(ProposalFailure.UnsubscriptionDeadlineExpired.class, exception.failure());
        assertTrue(proposta.isIscritto("mario"));
    }
}
