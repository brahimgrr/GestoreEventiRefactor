package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.authentication.dto.CredenzialiRequest;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.domain.utente.Configuratore;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.IMainView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu.IConfiguratoreView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {
    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ControllerFixture fixture = fixture(new MockView());

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new MainController(null, fixture.auth, fixture.configuratoreController, fixture.fruitoreController)),
                () -> assertThrows(NullPointerException.class,
                        () -> new MainController(fixture.view, null, fixture.configuratoreController, fixture.fruitoreController)),
                () -> assertThrows(NullPointerException.class,
                        () -> new MainController(fixture.view, fixture.auth, null, fixture.fruitoreController)),
                () -> assertThrows(NullPointerException.class,
                        () -> new MainController(fixture.view, fixture.auth, fixture.configuratoreController, null))
        );
    }

    @Test
    void run_conSceltaEsci_mostraAvvioEUscita() {
        MockView view = new MockView(IMainView.AccessAction.ESCI);

        fixture(view).controller.run();

        assertAll(
                () -> assertEquals(1, view.appStarted),
                () -> assertEquals(1, view.exitShown)
        );
    }

    @Test
    void run_conLoginFruitoreValido_avviaControllerFruitoreEMostraLogout() {
        MockView mainView = new MockView(IMainView.AccessAction.LOGIN_FRUITORE, IMainView.AccessAction.ESCI);
        mainView.fruitoreLogins.add(Optional.of(new CredenzialiRequest("mario", "pass")));
        ControllerFixture fixture = fixture(mainView);
        fixture.auth.registraNuovoFruitore("mario", "pass");

        fixture.controller.run();

        assertAll(
                () -> assertEquals(1, mainView.fruitoreLoginSuccess),
                () -> assertEquals(1, mainView.fruitoreLogoutShown)
        );
    }

    @Test
    void run_conLoginConfiguratoreValido_avviaControllerConfiguratoreEMostraLogout() {
        MockView view = new MockView(IMainView.AccessAction.LOGIN_CONFIGURATORE, IMainView.AccessAction.ESCI);
        view.configuratoreLogins.add(Optional.of(new CredenzialiRequest("admin", "pass")));
        ControllerFixture fixture = fixture(view);
        fixture.auth.registraNuovoConfiguratore("admin", "pass");

        fixture.controller.run();

        assertAll(
                () -> assertEquals(1, view.configuratoreLoginSuccess),
                () -> assertEquals(1, view.configuratoreLogoutShown),
                () -> assertEquals(1, view.exitShown)
        );
    }

    @Test
    void run_conLoginConfiguratoreAnnullato_tornaAlMenuAccesso() {
        MockView view = new MockView(IMainView.AccessAction.LOGIN_CONFIGURATORE, IMainView.AccessAction.ESCI);
        view.configuratoreLogins.add(Optional.empty());

        fixture(view).controller.run();

        assertAll(
                () -> assertEquals(0, view.configuratoreLogoutShown),
                () -> assertEquals(1, view.exitShown)
        );
    }

    @Test
    void run_conLoginFruitoreAnnullato_tornaAlMenuAccesso() {
        MockView view = new MockView(IMainView.AccessAction.LOGIN_FRUITORE, IMainView.AccessAction.ESCI);
        view.fruitoreLogins.add(Optional.empty());

        fixture(view).controller.run();

        assertAll(
                () -> assertEquals(0, view.fruitoreLogoutShown),
                () -> assertEquals(1, view.exitShown)
        );
    }

    @Test
    void run_conRegistrazioneFruitoreCompleta_prosegueFinoAUscita() {
        MockView view = new MockView(IMainView.AccessAction.REGISTRA_FRUITORE, IMainView.AccessAction.ESCI);
        view.fruitoreRegistrations.add(Optional.of(new CredenzialiRequest("anna", "pass")));

        fixture(view).controller.run();

        assertAll(
                () -> assertEquals(1, view.registrationCompleted),
                () -> assertEquals(1, view.exitShown)
        );
    }

    @Test
    void loginConfiguratore_conPrimoAccessoRegistraNuoveCredenziali() {
        MockView view = new MockView();
        view.configuratoreLogins.add(Optional.of(new CredenzialiRequest(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA)));
        view.configuratoreRegistrations.add(Optional.of(new CredenzialiRequest("nuovoConfig", "pass")));

        Configuratore configuratore = fixture(view).controller.loginConfiguratore();

        assertAll(
                () -> assertEquals("nuovoConfig", configuratore.getUsername()),
                () -> assertEquals(1, view.firstAccessShown),
                () -> assertEquals(1, view.registrationCompleted)
        );
    }

    @Test
    void loginConfiguratore_conCredenzialiRegistrate_restituisceConfiguratoreSenzaPrimoAccesso() {
        MockView view = new MockView();
        view.configuratoreLogins.add(Optional.of(new CredenzialiRequest("admin", "pass")));
        ControllerFixture fixture = fixture(view);
        fixture.auth.registraNuovoConfiguratore("admin", "pass");

        Configuratore configuratore = fixture.controller.loginConfiguratore();

        assertAll(
                () -> assertEquals("admin", configuratore.getUsername()),
                () -> assertEquals(1, view.configuratoreLoginSuccess),
                () -> assertEquals(0, view.firstAccessShown)
        );
    }

    @Test
    void loginConfiguratore_conPrimoAccessoAnnullato_mostraAnnullamentoERichiedeDiNuovoLogin() {
        MockView view = new MockView();
        view.configuratoreLogins.add(Optional.of(new CredenzialiRequest(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA)));
        view.configuratoreRegistrations.add(Optional.empty());
        view.configuratoreLogins.add(Optional.empty());

        Configuratore configuratore = fixture(view).controller.loginConfiguratore();

        assertAll(
                () -> assertNull(configuratore),
                () -> assertEquals(1, view.firstAccessShown),
                () -> assertEquals(1, view.registrationCancelled)
        );
    }

    @Test
    void loginConfiguratore_conRegistrazionePrimoAccessoErrataPoiValida_mostraErroreECompleta() {
        MockView view = new MockView();
        view.configuratoreLogins.add(Optional.of(new CredenzialiRequest(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA)));
        view.configuratoreRegistrations.add(Optional.of(new CredenzialiRequest(
                AuthenticationService.USERNAME_PREDEFINITO,
                "pass")));
        view.configuratoreRegistrations.add(Optional.of(new CredenzialiRequest("admin", "pass")));

        Configuratore configuratore = fixture(view).controller.loginConfiguratore();

        assertAll(
                () -> assertEquals("admin", configuratore.getUsername()),
                () -> assertEquals(1, view.errorsShown),
                () -> assertEquals(1, view.registrationCompleted)
        );
    }

    @Test
    void loginConfiguratore_conCredenzialiErratePoiAnnullamento_restituisceNull() {
        MockView view = new MockView();
        view.configuratoreLogins.add(Optional.of(new CredenzialiRequest("sconosciuto", "pass")));
        view.configuratoreLogins.add(Optional.empty());

        Configuratore configuratore = fixture(view).controller.loginConfiguratore();

        assertAll(
                () -> assertNull(configuratore),
                () -> assertEquals(1, view.loginFailed)
        );
    }

    @Test
    void loginFruitore_conCredenzialiErratePoiValide_mostraFallimentoPoiSuccesso() {
        MockView view = new MockView();
        view.fruitoreLogins.add(Optional.of(new CredenzialiRequest("anna", "sbagliata")));
        view.fruitoreLogins.add(Optional.of(new CredenzialiRequest("anna", "pass")));
        ControllerFixture fixture = fixture(view);
        fixture.auth.registraNuovoFruitore("anna", "pass");

        Fruitore fruitore = fixture.controller.loginFruitore();

        assertAll(
                () -> assertEquals("anna", fruitore.getUsername()),
                () -> assertEquals(1, view.loginFailed),
                () -> assertEquals(1, view.fruitoreLoginSuccess)
        );
    }

    @Test
    void registraFruitore_conErrorePoiSuccesso_mostraErroreECompleta() {
        MockView view = new MockView();
        view.fruitoreRegistrations.add(Optional.of(new CredenzialiRequest(
                AuthenticationService.USERNAME_PREDEFINITO,
                "pass")));
        view.fruitoreRegistrations.add(Optional.of(new CredenzialiRequest("anna", "pass")));

        Fruitore fruitore = fixture(view).controller.registraFruitore();

        assertAll(
                () -> assertEquals("anna", fruitore.getUsername()),
                () -> assertEquals(1, view.errorsShown),
                () -> assertEquals(1, view.registrationCompleted)
        );
    }

    @Test
    void registraFruitore_conInputVuoto_mostraRegistrazioneAnnullata() {
        MockView view = new MockView();
        view.fruitoreRegistrations.add(Optional.empty());

        Fruitore fruitore = fixture(view).controller.registraFruitore();

        assertAll(
                () -> assertNull(fruitore),
                () -> assertEquals(1, view.registrationCancelled)
        );
    }

    private ControllerFixture fixture(MockView view) {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repo =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        AuthenticationService auth = new AuthenticationService(repo);
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        ConfiguratoreService configuratoreService = new ConfiguratoreService(
                graph.catalogoService(),
                graph.propostaService(),
                new BatchImportService(graph.catalogoService(), graph.propostaService()));
        MockConfiguratoreView configuratoreView = new MockConfiguratoreView(IConfiguratoreView.MainAction.LOGOUT);
        MockFruitoreView fruitoreView = new MockFruitoreView(IFruitoreView.MainAction.LOGOUT);
        ConfiguratoreController configuratoreController =
                new ConfiguratoreController(configuratoreView, configuratoreService);
        FruitoreController fruitoreController =
                new FruitoreController(fruitoreView, graph.fruitoreService());
        return new ControllerFixture(
                view,
                auth,
                configuratoreController,
                fruitoreController,
                new MainController(view, auth, configuratoreController, fruitoreController));
    }

    private record ControllerFixture(
            MockView view,
            AuthenticationService auth,
            ConfiguratoreController configuratoreController,
            FruitoreController fruitoreController,
            MainController controller) {
    }
}
