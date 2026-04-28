package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.testsupport.DomainFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaDomainTest {
    @BeforeEach
    void setUp() {
        DomainFixtures.useFixedClock();
    }

    @AfterEach
    void tearDown() {
        DomainFixtures.resetClock();
    }

    @Test
    void UC13_chiaveIdentita_isCaseInsensitiveAndTrimmed() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(AppConstants.CAMPO_TITOLO, "  Giro Sul Lago ");
        values.put(AppConstants.CAMPO_DATA, "18/01/2026");
        values.put(AppConstants.CAMPO_ORA, "16:30");
        values.put(AppConstants.CAMPO_LUOGO, " BRESCIA ");

        assertEquals("giro sul lago|18/01/2026|16:30|brescia", Proposta.chiaveIdentita(values));
    }

    @Test
    void UC14_stateTransitions_followDocumentedLifecycle() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");

        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        proposta.setStato(StatoProposta.CONFERMATA);
        proposta.setStato(StatoProposta.CONCLUSA);

        assertEquals(StatoProposta.CONCLUSA, proposta.getStato());
        assertEquals(5, proposta.getStateHistory().size());
    }

    @Test
    void UC14_stateTransitions_invalidTransitionRejected() {
        Proposta proposta = DomainFixtures.draftProposal("Giro sul lago");

        assertThrows(IllegalStateException.class, () -> proposta.setStato(StatoProposta.APERTA));
    }

    @Test
    void UC16_addAderente_requiresOpenStateAndUniqueUserAndCapacity() {
        Proposta bozza = DomainFixtures.draftProposal("Bozza");
        assertThrows(IllegalStateException.class, () -> bozza.addAderente("mario"));

        Proposta proposta = DomainFixtures.openProposal("Aperta", 1);
        proposta.addAderente("mario");

        assertThrows(IllegalStateException.class, () -> proposta.addAderente("mario"));
        assertThrows(IllegalStateException.class, () -> proposta.addAderente("luisa"));
    }

    @Test
    void UC21_removeAderente_afterSubscriptionDeadline_rejected() {
        Proposta proposta = DomainFixtures.openProposal("Aperta", 2);
        proposta.addAderente("mario");
        proposta.setTermineIscrizione(DomainFixtures.TODAY.minusDays(1));

        assertThrows(IllegalStateException.class,
                () -> proposta.removeAderente("mario", LocalDate.of(2026, 1, 10)));
    }
}
