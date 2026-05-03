package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.bacheca.IscrizioneService;
import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.bacheca.StateTransitionService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.domain.Configuratore;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.persistence.file.FileBachecaRepository;
import it.unibs.ingsoft.persistence.file.FileCatalogoRepository;
import it.unibs.ingsoft.persistence.file.FileCredenzialiRepository;
import it.unibs.ingsoft.persistence.file.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.presentation.controller.MainController;
import it.unibs.ingsoft.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.presentation.controller.FruitoreController;
import it.unibs.ingsoft.presentation.view.cli.MainCliView;
import it.unibs.ingsoft.presentation.view.cli.ConfiguratoreCliView;
import it.unibs.ingsoft.presentation.view.cli.FruitoreCliView;
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
        MainController controller = mainController(view, service);

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
        MainController controller = mainController(view, service);

        Fruitore fruitore = controller.registraFruitore();

        assertEquals("mario", fruitore.getUsername());
        AuthenticationService reloaded = new AuthenticationService(new FileCredenzialiRepository(utenti));
        assertTrue(reloaded.loginFruitore("mario", "passw").isPresent());
    }

    private MainController mainController(ScriptedAppView view, AuthenticationService service) {
        FileBachecaRepository bachecaRepo = new FileBachecaRepository(tempDir.resolve("proposte.json"));
        FileSpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"));
        CatalogoService catalogoService = new CatalogoService(new FileCatalogoRepository(tempDir.resolve("catalogo.json")));
        PropostaService propostaService = new PropostaService(bachecaRepo);
        NotificationService notificationService = new NotificationService(spazioRepo);
        StateTransitionService stateService = new StateTransitionService(bachecaRepo, notificationService);
        IscrizioneService iscrizioneService = new IscrizioneService(bachecaRepo, stateService);
        BatchImportService batchImportService = new BatchImportService(catalogoService, propostaService);

        ConfiguratoreController configuratoreController = new ConfiguratoreController(
                new ConfiguratoreCliView(view),
                catalogoService,
                propostaService,
                stateService,
                batchImportService);
        FruitoreController fruitoreController = new FruitoreController(
                new FruitoreCliView(view),
                propostaService,
                iscrizioneService,
                notificationService);

        return new MainController(
                new MainCliView(view),
                service,
                configuratoreController,
                fruitoreController);
    }
}
