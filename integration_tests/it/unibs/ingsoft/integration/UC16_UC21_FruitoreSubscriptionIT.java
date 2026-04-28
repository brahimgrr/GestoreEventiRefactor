package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.IscrizioneService;
import it.unibs.ingsoft.application.NotificationService;
import it.unibs.ingsoft.application.StateTransitionService;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.persistence.impl.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UC16_UC21_FruitoreSubscriptionIT {
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
    void UC16_UC21_subscribePreventDuplicateUnsubscribeAndRejectExpiredUnsubscribe() {
        Path proposteFile = tempDir.resolve("proposte.json");
        Path notificheFile = tempDir.resolve("notifiche.json");
        FileBachecaRepository bachecaRepo = new FileBachecaRepository(proposteFile);
        FileSpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(notificheFile);
        StateTransitionService stateService = new StateTransitionService(bachecaRepo, new NotificationService(spazioRepo));
        IscrizioneService service = new IscrizioneService(bachecaRepo, stateService);
        Fruitore mario = new Fruitore("mario");
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);
        bachecaRepo.get().addProposta(proposta);
        bachecaRepo.save();

        service.iscrivi(proposta, mario);
        assertTrue(proposta.getListaAderenti().contains("mario"));
        assertThrows(IllegalStateException.class, () -> service.iscrivi(proposta, mario));

        service.disiscrivi(proposta, mario);
        assertFalse(proposta.getListaAderenti().contains("mario"));

        Proposta expired = DomainFixtures.openProposal("Iscrizione scaduta", 3);
        expired.addAderente("mario");
        expired.setTermineIscrizione(DomainFixtures.TODAY.minusDays(1));
        bachecaRepo.get().addProposta(expired);
        bachecaRepo.save();

        assertThrows(IllegalStateException.class, () -> service.disiscrivi(expired, mario));
        assertTrue(expired.getListaAderenti().contains("mario"));
    }
}
