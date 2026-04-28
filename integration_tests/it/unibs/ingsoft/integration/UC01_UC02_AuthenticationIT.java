package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.AuthenticationService;
import it.unibs.ingsoft.domain.Configuratore;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.presentation.controller.AuthController;
import it.unibs.ingsoft.testsupport.ScriptedAppView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UC01_UC02_AuthenticationIT {
    @TempDir
    Path tempDir;

    @Test
    void UC01_UC02_configuratoreDefaultLoginForcesPersonalRegistrationAndPersists() {
        Path utenti = tempDir.resolve("utenti.json");
        AuthenticationService service = new AuthenticationService(new FileCredenzialiRepository(utenti));
        ScriptedAppView view = new ScriptedAppView()
                .strings("config", "config", "admin", "passw")
                .booleans(true);
        AuthController controller = new AuthController(view, service);

        Configuratore configuratore = controller.loginConfiguratore();

        assertEquals("admin", configuratore.getUsername());
        AuthenticationService reloaded = new AuthenticationService(new FileCredenzialiRepository(utenti));
        assertTrue(reloaded.login("admin", "passw").isPresent());
    }

    @Test
    void UC02_fruitoreRegistrationPersistsAndEnablesLogin() {
        Path utenti = tempDir.resolve("utenti.json");
        AuthenticationService service = new AuthenticationService(new FileCredenzialiRepository(utenti));
        ScriptedAppView view = new ScriptedAppView()
                .strings("mario", "passw")
                .booleans(true);
        AuthController controller = new AuthController(view, service);

        Fruitore fruitore = controller.registraFruitore();

        assertEquals("mario", fruitore.getUsername());
        AuthenticationService reloaded = new AuthenticationService(new FileCredenzialiRepository(utenti));
        assertTrue(reloaded.loginFruitore("mario", "passw").isPresent());
    }
}
