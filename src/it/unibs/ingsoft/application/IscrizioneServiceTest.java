package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import it.unibs.ingsoft.testsupport.InMemoryBachecaRepository;
import it.unibs.ingsoft.testsupport.InMemorySpazioPersonaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IscrizioneServiceTest {
    private InMemoryBachecaRepository bachecaRepo;
    private InMemorySpazioPersonaleRepository spazioRepo;
    private IscrizioneService service;

    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
        bachecaRepo = new InMemoryBachecaRepository();
        spazioRepo = new InMemorySpazioPersonaleRepository();
        NotificationService notificationService = new NotificationService(spazioRepo);
        StateTransitionService stateService = new StateTransitionService(bachecaRepo, notificationService);
        service = new IscrizioneService(bachecaRepo, stateService);
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC16_iscrivi_success_addsFruitoreAndPersists() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);
        Fruitore fruitore = new Fruitore("mario");

        service.iscrivi(proposta, fruitore);

        assertTrue(proposta.getListaAderenti().contains("mario"));
        assertEquals(1, bachecaRepo.saveCount());
    }

    @Test
    void UC16_iscrivi_duplicateSubscription_rejected() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);
        Fruitore fruitore = new Fruitore("mario");
        service.iscrivi(proposta, fruitore);

        assertThrows(IllegalStateException.class, () -> service.iscrivi(proposta, fruitore));
        assertEquals(1, bachecaRepo.saveCount());
    }

    @Test
    void UC16_iscrivi_whenCapacityReached_confirmsProposalAndNotifiesAderente() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 1);
        Fruitore fruitore = new Fruitore("mario");

        service.iscrivi(proposta, fruitore);

        assertEquals(StatoProposta.CONFERMATA, proposta.getStato());
        assertEquals(1, spazioRepo.get("mario").getNotifiche().size());
        assertEquals(1, bachecaRepo.saveCount());
    }

    @Test
    void UC16_iscrivi_afterDeadline_rejected() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);
        proposta.setTermineIscrizione(DomainFixtures.TODAY.minusDays(1));

        assertThrows(IllegalStateException.class,
                () -> service.iscrivi(proposta, new Fruitore("mario")));
        assertTrue(proposta.getListaAderenti().isEmpty());
    }

    @Test
    void UC21_disiscrivi_success_removesFruitoreAndPersists() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);
        Fruitore fruitore = new Fruitore("mario");
        service.iscrivi(proposta, fruitore);

        service.disiscrivi(proposta, fruitore);

        assertFalse(proposta.getListaAderenti().contains("mario"));
        assertEquals(2, bachecaRepo.saveCount());
    }

    @Test
    void UC21_disiscrivi_notSubscribed_rejected() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);

        assertThrows(IllegalStateException.class,
                () -> service.disiscrivi(proposta, new Fruitore("mario")));
    }

    @Test
    void UC21_disiscrivi_afterDeadline_rejectedWithoutPersisting() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 3);
        Fruitore fruitore = new Fruitore("mario");
        service.iscrivi(proposta, fruitore);
        proposta.setTermineIscrizione(DomainFixtures.TODAY.minusDays(1));

        assertThrows(IllegalStateException.class, () -> service.disiscrivi(proposta, fruitore));

        assertTrue(proposta.getListaAderenti().contains("mario"));
        assertEquals(1, bachecaRepo.saveCount());
    }
}
