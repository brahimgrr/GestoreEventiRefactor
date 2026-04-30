package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.domain.Configuratore;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.testsupport.InMemoryCredenzialiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {
    private InMemoryCredenzialiRepository repo;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCredenzialiRepository();
        service = new AuthenticationService(repo);
    }

    @Test
    void UC01_loginConfiguratore_registeredCredentials_success() {
        repo.get().addConfiguratore("admin", "pass");

        Optional<Configuratore> result = service.login("admin", "pass");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
    }

    @Test
    void UC01_loginConfiguratore_defaultCredentials_success() {
        Optional<Configuratore> result = service.login("config", "config");

        assertTrue(result.isPresent());
        assertEquals(AuthenticationService.USERNAME_PREDEFINITO, result.get().getUsername());
    }

    @Test
    void UC01_loginFruitore_registeredCredentials_success() {
        repo.get().addFruitore("mario", "pass");

        Optional<Fruitore> result = service.loginFruitore("mario", "pass");

        assertTrue(result.isPresent());
        assertEquals("mario", result.get().getUsername());
    }

    @Test
    void UC01_loginInvalidCredentials_returnsEmpty() {
        repo.get().addConfiguratore("admin", "pass");

        assertTrue(service.login("admin", "wrong").isEmpty());
        assertTrue(service.login("unknown", "pass").isEmpty());
        assertTrue(service.login(null, "pass").isEmpty());
    }

    @Test
    void UC02_registraNuovoConfiguratore_success_persistsAndCanLogin() {
        Configuratore registered = service.registraNuovoConfiguratore("admin", "pass");

        assertEquals("admin", registered.getUsername());
        assertEquals(1, repo.saveCount());
        assertTrue(service.login("admin", "pass").isPresent());
    }

    @Test
    void UC02_registraNuovoFruitore_success_persistsAndCanLogin() {
        Fruitore registered = service.registraNuovoFruitore("mario", "pass");

        assertEquals("mario", registered.getUsername());
        assertEquals(1, repo.saveCount());
        assertTrue(service.loginFruitore("mario", "pass").isPresent());
    }

    @Test
    void UC02_registraNuovoAccount_duplicateUsernameAcrossRoles_rejected() {
        service.registraNuovoFruitore("mario", "pass");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registraNuovoConfiguratore("MARIO", "pass"));

        assertTrue(ex.getMessage().contains("Esiste"));
        assertEquals(1, repo.saveCount());
    }

    @Test
    void UC02_registraNuovoAccount_reservedDefaultUsername_rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registraNuovoConfiguratore("config", "pass"));

        assertTrue(ex.getMessage().contains("riservato"));
        assertEquals(0, repo.saveCount());
    }

    @Test
    void UC02_registraNuovoAccount_invalidUsernameOrPassword_rejected() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.registraNuovoFruitore("ab", "pass")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.registraNuovoFruitore("mario", "abc")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.registraNuovoFruitore("   ", "pass"))
        );
        assertEquals(0, repo.saveCount());
    }
}
