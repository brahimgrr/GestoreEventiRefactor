package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import it.unibs.ingsoft.testsupport.InMemoryBachecaRepository;
import it.unibs.ingsoft.testsupport.InMemorySpazioPersonaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionServiceTest {
    private InMemoryBachecaRepository bachecaRepo;
    private InMemorySpazioPersonaleRepository spazioRepo;
    private StateTransitionService service;

    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
        bachecaRepo = new InMemoryBachecaRepository();
        spazioRepo = new InMemorySpazioPersonaleRepository();
        service = new StateTransitionService(bachecaRepo, new NotificationService(spazioRepo));
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC20_ritiraProposta_openBeforeEvent_successStateNotificationAndSave() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 2);
        proposta.addAderente("mario");
        bachecaRepo.get().addProposta(proposta);

        service.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(1, spazioRepo.get("mario").getNotifiche().size());
        assertEquals(1, bachecaRepo.saveCount());
    }

    @Test
    void UC20_ritiraProposta_onEventDate_rejected() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 2);
        proposta.setDataEvento(DomainFixtures.TODAY);

        assertThrows(IllegalStateException.class, () -> service.ritiraProposta(proposta));
        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(0, bachecaRepo.saveCount());
    }

    @Test
    void UC20_ritiraProposta_nonOpenOrConfirmedState_rejected() {
        Proposta proposta = DomainFixtures.draftProposal("Bozza");

        assertThrows(IllegalStateException.class, () -> service.ritiraProposta(proposta));
    }

    @Test
    void automaticTransition_openFullAfterDeadline_confirmsAndNotifies() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 1);
        proposta.addAderente("mario");
        proposta.setTermineIscrizione(DomainFixtures.TODAY.minusDays(1));
        bachecaRepo.get().addProposta(proposta);

        service.controllaScadenze();

        assertEquals(StatoProposta.CONFERMATA, proposta.getStato());
        assertEquals(1, spazioRepo.get("mario").getNotifiche().size());
        assertEquals(1, bachecaRepo.saveCount());
    }

    @Test
    void automaticTransition_openNotFullAfterDeadline_annullaAndNotifies() {
        Proposta proposta = DomainFixtures.openProposal("Giro sul lago", 2);
        proposta.addAderente("mario");
        proposta.setTermineIscrizione(DomainFixtures.TODAY.minusDays(1));
        bachecaRepo.get().addProposta(proposta);

        service.controllaScadenze();

        assertEquals(StatoProposta.ANNULLATA, proposta.getStato());
        assertEquals(1, spazioRepo.get("mario").getNotifiche().size());
        assertEquals(1, bachecaRepo.saveCount());
    }

    @Test
    void automaticTransition_confirmedAfterConclusionDate_concludes() {
        Proposta proposta = new Proposta(DomainFixtures.category("Escursione"),
                DomainFixtures.baseFields(), DomainFixtures.commonFields());
        proposta.putAllValoriCampi(DomainFixtures.validProposalValues(
                "Giro sul lago",
                1,
                DomainFixtures.TODAY.minusDays(4),
                DomainFixtures.TODAY.minusDays(2),
                DomainFixtures.TODAY.minusDays(1)));
        proposta.setTermineIscrizione(DomainFixtures.TODAY.minusDays(4));
        proposta.setDataEvento(DomainFixtures.TODAY.minusDays(2));
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        proposta.addAderente("mario");
        proposta.setStato(StatoProposta.CONFERMATA);
        bachecaRepo.get().addProposta(proposta);

        service.controllaScadenze();

        assertEquals(StatoProposta.CONCLUSA, proposta.getStato());
        assertEquals(1, bachecaRepo.saveCount());
    }
}
