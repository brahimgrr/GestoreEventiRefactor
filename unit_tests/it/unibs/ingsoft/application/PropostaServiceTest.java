package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.testsupport.DomainFixtures;
import it.unibs.ingsoft.testsupport.InMemoryBachecaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaServiceTest {
    private InMemoryBachecaRepository repo;
    private PropostaService service;

    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
        repo = new InMemoryBachecaRepository();
        service = new PropostaService(repo);
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC13_creareProposta_validFields_setsStateVALIDAAndDates() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");

        List<String> errors = service.validaProposta(proposta);

        assertTrue(errors.isEmpty());
        assertEquals(StatoProposta.VALIDA, proposta.getStato());
        assertEquals(DomainFixtures.TODAY.plusDays(5), proposta.getTermineIscrizione());
        assertEquals(DomainFixtures.TODAY.plusDays(8), proposta.getDataEvento());
    }

    @Test
    void UC13_creareProposta_missingMandatoryField_returnsErrorsAndKeepsBozza() {
        Proposta proposta = new Proposta(DomainFixtures.category("Escursione"),
                DomainFixtures.baseFields(), DomainFixtures.commonFields());
        Map<String, String> values = new java.util.LinkedHashMap<>(DomainFixtures.validProposalValues("Giro sul lago"));
        values.remove(AppConstants.CAMPO_TITOLO);
        proposta.putAllValoriCampi(values);

        List<String> errors = service.validaProposta(proposta);

        assertFalse(errors.isEmpty());
        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(errors.stream().anyMatch(e -> e.contains(AppConstants.CAMPO_TITOLO)));
    }

    @Test
    void UC13_creareProposta_invalidTemporalRules_returnsDateErrors() {
        Proposta proposta = new Proposta(DomainFixtures.category("Escursione"),
                DomainFixtures.baseFields(), DomainFixtures.commonFields());
        proposta.putAllValoriCampi(DomainFixtures.validProposalValues(
                "Date sbagliate",
                3,
                DomainFixtures.TODAY,
                DomainFixtures.TODAY.plusDays(1),
                DomainFixtures.TODAY.minusDays(1)));

        List<String> errors = service.validaProposta(proposta);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(errors.stream().anyMatch(e -> e.contains(AppConstants.CAMPO_TERMINE_ISCRIZIONE)));
        assertTrue(errors.stream().anyMatch(e -> e.contains(AppConstants.CAMPO_DATA)));
        assertTrue(errors.stream().anyMatch(e -> e.contains(AppConstants.CAMPO_DATA_CONCLUSIVA)));
    }

    @Test
    void UC13_incrementalFieldValidation_rejectsExpiredTermineIscrizione() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");

        List<String> errors = service.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                DomainFixtures.TODAY.minusDays(1).format(AppConstants.DATE_FMT));

        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("successivo"));
    }

    @Test
    void UC14_salvaProposta_validProposal_successAndDuplicateInMemoryRejected() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");
        assertTrue(service.validaProposta(proposta).isEmpty());
        Proposta duplicate = DomainFixtures.draftProposal("Giro sul lago");
        assertTrue(service.validaProposta(duplicate).isEmpty());

        service.salvaProposta(proposta);

        assertEquals(1, service.getProposteValide().size());
        assertThrows(IllegalStateException.class, () -> service.salvaProposta(duplicate));
    }

    @Test
    void UC14_pubblicaProposta_success_setsAPERTAAndPersistsInBacheca() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");
        assertTrue(service.validaProposta(proposta).isEmpty());

        service.pubblicaProposta(proposta);

        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(DomainFixtures.TODAY, proposta.getDataPubblicazione());
        assertEquals(1, repo.get().getProposte().size());
        assertEquals(1, repo.saveCount());
    }

    @Test
    void UC14_pubblicaProposta_expiredTermine_rejectedAndNotPersisted() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");
        assertTrue(service.validaProposta(proposta).isEmpty());
        proposta.setTermineIscrizione(LocalDate.of(2026, 1, 9));

        assertThrows(IllegalStateException.class, () -> service.pubblicaProposta(proposta));

        assertTrue(repo.get().getProposte().isEmpty());
        assertEquals(0, repo.saveCount());
    }

    @Test
    void UC14_pubblicaProposta_duplicatePublishedProposal_rejected() {
        Proposta first = DomainFixtures.draftProposal("Giro sul lago");
        Proposta duplicate = DomainFixtures.draftProposal("Giro sul lago");
        assertTrue(service.validaProposta(first).isEmpty());
        assertTrue(service.validaProposta(duplicate).isEmpty());
        service.pubblicaProposta(first);

        assertThrows(IllegalStateException.class, () -> service.pubblicaProposta(duplicate));

        assertEquals(1, repo.get().getProposte().size());
    }

    @Test
    void UC15_visualizzareBacheca_returnsOnlyOpenProposalsGroupedByCategoria() {
        Proposta open = DomainFixtures.openProposal("Aperta", 3);
        Proposta draft = DomainFixtures.draftProposal("Bozza");
        repo.get().addProposta(open);
        repo.get().addProposta(draft);

        Map<String, List<Proposta>> grouped = service.getBachecaPerCategoria();

        assertEquals(1, grouped.size());
        assertEquals(List.of(open), grouped.get("Escursione"));
    }
}
