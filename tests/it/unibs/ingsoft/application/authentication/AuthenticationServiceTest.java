package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.error.ApplicationException;
import it.unibs.ingsoft.shared.error.Failure;
import it.unibs.ingsoft.domain.model.utente.Configuratore;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.domain.model.utente.PasswordHash;
import it.unibs.ingsoft.domain.model.utente.UtenteFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {
    @Test
    void registraNuovoConfiguratore_eLoginConfiguratore_conRepositoryCondiviso() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        AuthenticationService service = new AuthenticationService(repository);

        Configuratore registrato = service.registraNuovoConfiguratore("AdminUno", "pass1234");
        Optional<Configuratore> login = service.login("adminuno", "pass1234");

        assertAll(
                () -> assertEquals("AdminUno", registrato.getUsername()),
                () -> assertTrue(login.isPresent()),
                () -> assertEquals("adminuno", login.get().getUsername().toLowerCase()),
                () -> assertEquals(1, repository.saveCount())
        );
    }

    @Test
    void registraNuovoAccount_salvaHashSha256SempliceSenzaPasswordInChiaro() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        AuthenticationService service = new AuthenticationService(repository);

        service.registraNuovoConfiguratore("AdminUno", "pass1234");

        PasswordHash hash = repository.findByUsername("adminuno").orElseThrow().passwordHash();
        assertAll(
                () -> assertFalse(hash.hash().isBlank()),
                () -> assertNotEquals("pass1234", hash.hash())
        );
    }

    @Test
    void registraNuovoFruitore_eLoginFruitore_conRepositoryCondiviso() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        AuthenticationService service = new AuthenticationService(repository);

        Fruitore registrato = service.registraNuovoFruitore("Mario", "pass1234");
        Optional<Fruitore> login = service.loginFruitore("mario", "pass1234");

        assertAll(
                () -> assertEquals("Mario", registrato.getUsername()),
                () -> assertTrue(login.isPresent()),
                () -> assertEquals("mario", login.get().getUsername().toLowerCase()),
                () -> assertTrue(service.esisteUsername("MARIO"))
        );
    }

    @Test
    void costruttori_conDipendenzeNull_lancianoNullPointerException() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new AuthenticationService(null)),
                () -> assertThrows(NullPointerException.class, () -> new AuthenticationService(repository, null))
        );
    }

    @Test
    void login_conCredenzialiPredefinite_restituisceConfiguratorePredefinito() {
        AuthenticationService service = serviceVuoto();

        Optional<Configuratore> login = service.login(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA);

        assertAll(
                () -> assertTrue(login.isPresent()),
                () -> assertEquals(AuthenticationService.USERNAME_PREDEFINITO, login.get().getUsername()),
                () -> assertTrue(service.isConfiguratorePredefinito(login.get()))
        );
    }

    @Test
    void login_conCredenzialiNonValide_restituisceEmpty() {
        AuthenticationService service = serviceVuoto();
        service.registraNuovoConfiguratore("AdminUno", "pass1234");
        service.registraNuovoFruitore("Mario", "pass5678");

        assertAll(
                () -> assertTrue(service.login(null, "pass1234").isEmpty()),
                () -> assertTrue(service.login("admin", null).isEmpty()),
                () -> assertTrue(service.login("AdminUno", "sbagliata").isEmpty()),
                () -> assertTrue(service.loginFruitore(null, "pass1234").isEmpty()),
                () -> assertTrue(service.loginFruitore("Mario", null).isEmpty()),
                () -> assertTrue(service.loginFruitore("Mario", "sbagliata").isEmpty())
        );
    }

    @Test
    void login_conUsernameConSpazi_usaChiaveNormalizzataMaMantieneUsernameCreato() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        AuthenticationService service = new AuthenticationService(repository);
        service.registraNuovoConfiguratore("AdminUno", "pass1234");
        service.registraNuovoFruitore("Mario", "pass5678");

        Optional<Configuratore> configuratore = service.login("  ADMINUNO  ", "pass1234");
        Optional<Fruitore> fruitore = service.loginFruitore("  MARIO  ", "pass5678");

        assertAll(
                () -> assertTrue(configuratore.isPresent()),
                () -> assertEquals("ADMINUNO", configuratore.get().getUsername().trim()),
                () -> assertTrue(fruitore.isPresent()),
                () -> assertEquals("MARIO", fruitore.get().getUsername().trim())
        );
    }

    @Test
    void registraNuovoAccount_conValoriInvalidi_lanciaApplicationExceptionConFailureCorrente() {
        AuthenticationService service = serviceVuoto();

        assertAll(
                () -> assertApplicationFailure(AuthenticationFailure.UsernameInvalid.class,
                        () -> service.registraNuovoConfiguratore(null, "pass1234")),
                () -> assertApplicationFailure(AuthenticationFailure.UsernameInvalid.class,
                        () -> service.registraNuovoConfiguratore("   ", "pass1234")),
                () -> assertApplicationFailure(AuthenticationFailure.UsernameTooShort.class,
                        () -> service.registraNuovoConfiguratore("ab", "pass1234")),
                () -> assertApplicationFailure(AuthenticationFailure.PasswordInvalid.class,
                        () -> service.registraNuovoConfiguratore("AdminUno", null)),
                () -> assertApplicationFailure(AuthenticationFailure.PasswordInvalid.class,
                        () -> service.registraNuovoConfiguratore("AdminUno", "   ")),
                () -> assertApplicationFailure(AuthenticationFailure.PasswordTooShort.class,
                        () -> service.registraNuovoConfiguratore("AdminUno", "abc"))
        );
    }

    @Test
    void registraNuovoAccount_conUsernameRiservatoODuplicato_lanciaFailureCorrente() {
        AuthenticationService service = serviceVuoto();
        service.registraNuovoFruitore("UtenteComune", "pass1234");

        assertAll(
                () -> assertApplicationFailure(AuthenticationFailure.UsernameReserved.class,
                        () -> service.registraNuovoConfiguratore("config", "pass1234")),
                () -> assertApplicationFailure(AuthenticationFailure.UsernameReserved.class,
                        () -> service.registraNuovoFruitore("CONFIG", "pass1234")),
                () -> assertApplicationFailure(AuthenticationFailure.UsernameAlreadyInUse.class,
                        () -> service.registraNuovoConfiguratore("utentecomune", "pass5678"))
        );
    }

    @Test
    void validaNuovoUsernameEPassword_restituisconoFailureSenzaLanciare() {
        AuthenticationService service = serviceVuoto();
        service.registraNuovoFruitore("Mario", "pass1234");

        assertAll(
                () -> assertOptionalFailure(AuthenticationFailure.UsernameTooShort.class,
                        service.validaNuovoUsername(null)),
                () -> assertOptionalFailure(AuthenticationFailure.UsernameTooShort.class,
                        service.validaNuovoUsername("ab")),
                () -> assertOptionalFailure(AuthenticationFailure.UsernameReserved.class,
                        service.validaNuovoUsername(" config ")),
                () -> assertOptionalFailure(AuthenticationFailure.UsernameAlreadyInUse.class,
                        service.validaNuovoUsername("mario")),
                () -> assertTrue(service.validaNuovoUsername("Luigi").isEmpty()),
                () -> assertOptionalFailure(AuthenticationFailure.PasswordTooShort.class,
                        service.validaNuovaPassword("abc")),
                () -> assertTrue(service.validaNuovaPassword("abcd").isEmpty())
        );
    }

    @Test
    void registrazione_normalizzaUsernamePrimaDelSalvataggio() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        AuthenticationService service = new AuthenticationService(repository);

        Configuratore configuratore = service.registraNuovoConfiguratore("  AdminUno  ", "pass1234");
        Fruitore fruitore = service.registraNuovoFruitore("  Mario  ", "pass5678");

        assertAll(
                () -> assertEquals("AdminUno", configuratore.getUsername()),
                () -> assertEquals("Mario", fruitore.getUsername()),
                () -> assertTrue(repository.existsByUsername("adminuno")),
                () -> assertTrue(repository.existsByUsername("mario")),
                () -> assertFalse(service.esisteUsername(null)),
                () -> assertFalse(service.isConfiguratorePredefinito(null)),
                () -> assertFalse(service.isConfiguratorePredefinito(UtenteFactory.getInstance().creaConfiguratore("admin")))
        );
    }

    private AuthenticationService serviceVuoto() {
        return new AuthenticationService(new ApplicationIntegrationSupport.InMemoryCredenzialiRepository());
    }

    private void assertApplicationFailure(Class<? extends Failure> expected, Runnable action) {
        ApplicationException exception = assertThrows(ApplicationException.class, action::run);

        assertInstanceOf(expected, exception.failure());
    }

    private void assertOptionalFailure(Class<? extends Failure> expected, Optional<Failure> failure) {
        assertTrue(failure.isPresent());
        assertInstanceOf(expected, failure.orElseThrow());
    }
}
