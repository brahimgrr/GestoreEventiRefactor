package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.persistence.file.FileBachecaRepository;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UC13_UC15_ProposalPublicationIT {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC13_UC14_UC15_validProposalCanBeSavedPublishedAndDisplayedInBacheca() {
        Path proposteFile = tempDir.resolve("proposte.json");
        PropostaService service = new PropostaService(new FileBachecaRepository(proposteFile));
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");

        assertTrue(service.validaProposta(proposta).isEmpty());
        service.salvaProposta(proposta);
        service.pubblicaProposta(proposta);
        service.rimuoviPropostaValida(proposta);

        PropostaService reloaded = new PropostaService(new FileBachecaRepository(proposteFile));
        Map<String, List<Proposta>> bacheca = reloaded.getBachecaPerCategoria();

        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(1, bacheca.size());
        assertEquals(1, bacheca.get("Escursione").size());
        assertEquals("Giro sul lago", bacheca.get("Escursione").get(0).getValoriCampi().get("Titolo"));
    }
}
