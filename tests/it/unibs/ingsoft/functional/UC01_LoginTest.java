package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.domain.Configuratore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC01_LoginTest {
    @Test
    void scenarioPrincipaleConfiguratore_credenzialiPersonaliValideECampiBaseFissati_accedeAlMenuPrincipaleConfiguratore() {
        FunctionalTestSupport.AuthenticationContext auth = FunctionalTestSupport.authenticationContext();
        auth.service().registraNuovoConfiguratore("admin", "pass1234");
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(java.util.List.of());

        assertAll(
                () -> assertTrue(auth.service().login("admin", "pass1234").isPresent()),
                () -> assertFalse(graph.configuratoreService().isPrimaConfigurazioneNecessaria())
        );
    }

    @Test
    void scenarioPrincipaleFruitore_credenzialiPersonaliValide_accedeAlMenuPrincipaleFruitore() {
        FunctionalTestSupport.AuthenticationContext auth = FunctionalTestSupport.authenticationContext();
        auth.service().registraNuovoFruitore("mario", "123456");

        assertTrue(auth.service().loginFruitore("mario", "123456").isPresent());
    }

    @Test
    void scenarioAlternativo6a_credenzialiPredefinite_riconosceConfiguratorePredefinito() {
        AuthenticationService service = FunctionalTestSupport.authenticationContext().service();

        Configuratore configuratore = service.login("config", "config").orElseThrow();

        assertTrue(service.isConfiguratorePredefinito(configuratore));
    }

    @Test
    void scenarioAlternativo6b_credenzialiErrate_nonAutentica() {
        AuthenticationService service = FunctionalTestSupport.authenticationContext().service();

        assertTrue(service.login("admin", "errata").isEmpty());
    }

    @Test
    void scenarioAlternativo8a_campiBaseNonFissati_rilevaPrimaConfigurazioneNecessaria() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();

        assertTrue(graph.configuratoreService().isPrimaConfigurazioneNecessaria());
    }
}
