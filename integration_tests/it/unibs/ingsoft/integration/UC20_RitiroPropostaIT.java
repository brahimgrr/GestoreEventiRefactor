package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.application.bacheca.StateTransitionService;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.persistence.file.FileBachecaRepository;
import it.unibs.ingsoft.persistence.file.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UC20_RitiroPropostaIT {
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
    void UC20_withdrawOpenProposalPersistsStateAndNotifications() {
        Path proposteFile = tempDir.resolve("proposte.json");
        Path notificheFile = tempDir.resolve("notifiche.json");
        FileBachecaRepository bachecaRepo = new FileBachecaRepository(proposteFile);
        FileSpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(notificheFile);
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 2);
        proposta.addAderente("mario");
        bachecaRepo.get().addProposta(proposta);
        bachecaRepo.save();
        StateTransitionService service =
                new StateTransitionService(bachecaRepo, new NotificationService(spazioRepo));

        service.ritiraProposta(proposta);

        FileBachecaRepository reloadedBacheca = new FileBachecaRepository(proposteFile);
        FileSpazioPersonaleRepository reloadedSpazio = new FileSpazioPersonaleRepository(notificheFile);
        assertEquals(StatoProposta.RITIRATA, reloadedBacheca.get().getProposte().get(0).getStato());
        assertEquals(1, reloadedSpazio.get("mario").getNotifiche().size());
    }
}
