package it.unibs.ingsoft.composition;

import it.unibs.ingsoft.application.bacheca.IscrizioneService;
import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.bacheca.StateTransitionService;
import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.FruitoreService;
import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.catalogo.CampoCatalogoService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.catalogo.CategoriaCatalogoService;
import it.unibs.ingsoft.application.proposta.PropostaCreationService;
import it.unibs.ingsoft.application.proposta.PropostaPublicationService;
import it.unibs.ingsoft.application.proposta.PropostaQueryService;
import it.unibs.ingsoft.application.proposta.PropostaValidationService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.factory.CampoFactory;
import it.unibs.ingsoft.domain.factory.NotificaFactory;
import it.unibs.ingsoft.domain.factory.PropostaFactory;
import it.unibs.ingsoft.domain.factory.UtenteFactory;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;
import it.unibs.ingsoft.persistence.file.FileRepositoryFactory;
import it.unibs.ingsoft.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.presentation.controller.FruitoreController;
import it.unibs.ingsoft.presentation.controller.MainController;
import it.unibs.ingsoft.presentation.view.cli.ConfiguratoreCliView;
import it.unibs.ingsoft.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.presentation.view.cli.FruitoreCliView;
import it.unibs.ingsoft.presentation.view.cli.MainCliView;
import it.unibs.ingsoft.presentation.view.interfaces.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.IConfiguratoreView;
import it.unibs.ingsoft.presentation.view.interfaces.IFruitoreView;
import it.unibs.ingsoft.presentation.view.interfaces.IMainView;

import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Composition root: crea e collega tutti i componenti, avvia il ciclo applicativo
 * e gestisce lo scheduler notturno per le transizioni automatiche di stato.
 */
public final class Application {
    private ScheduledExecutorService midnightScheduler;

    public void start() {
        CampoFactory campoFactory = new CampoFactory();
        PropostaFactory propostaFactory = new PropostaFactory();
        NotificaFactory notificaFactory = new NotificaFactory();
        UtenteFactory utenteFactory = new UtenteFactory();

        ICatalogoRepository catalogoRepo = FileRepositoryFactory
                .getInstance().createCatalogoRepository();
        ICredenzialiRepository credenzialiRepo = FileRepositoryFactory
                .getInstance().createCredenzialiRepository();
        IBachecaRepository propostaRepo = FileRepositoryFactory
                .getInstance().createBachecaRepository();
        ISpazioPersonaleRepository spazioRepo = FileRepositoryFactory
                .getInstance().createSpazioPersonaleRepository();

        CampoCatalogoService campoCatalogoService = new CampoCatalogoService(catalogoRepo, campoFactory);
        CategoriaCatalogoService categoriaCatalogoService = new CategoriaCatalogoService(catalogoRepo);
        CatalogoService catalogoService = new CatalogoService(campoCatalogoService, categoriaCatalogoService);

        PropostaCreationService propostaCreationService = new PropostaCreationService(propostaFactory);
        PropostaValidationService propostaValidationService = new PropostaValidationService();
        PropostaQueryService propostaQueryService = new PropostaQueryService(propostaRepo);
        PropostaPublicationService propostaPublicationService =
                new PropostaPublicationService(propostaRepo, propostaQueryService);
        PropostaService propostaService = new PropostaService(
                propostaCreationService,
                propostaValidationService,
                propostaPublicationService,
                propostaQueryService);

        AuthenticationService authService = new AuthenticationService(credenzialiRepo, utenteFactory);
        NotificationService notifService = new NotificationService(spazioRepo);
        StateTransitionService stateService = new StateTransitionService(propostaRepo, notifService, notificaFactory);
        IscrizioneService iscrizioneService = new IscrizioneService(propostaRepo, stateService);

        BatchImportService batchImportService = new BatchImportService(catalogoService, propostaService);
        ConfiguratoreService configuratoreService = new ConfiguratoreService(
                catalogoService,
                propostaService,
                stateService,
                batchImportService);
        FruitoreService fruitoreService = new FruitoreService(
                propostaService,
                iscrizioneService,
                notifService);

        IAppView ui = new ConsoleUI(new Scanner(System.in));
        IMainView mainView = new MainCliView(ui);
        IConfiguratoreView configuratoreView = new ConfiguratoreCliView(ui);
        IFruitoreView fruitoreView = new FruitoreCliView(ui);

        ConfiguratoreController configuratoreController = new ConfiguratoreController(
                configuratoreView,
                configuratoreService);

        FruitoreController fruitoreController = new FruitoreController(
                fruitoreView,
                fruitoreService);

        MainController mainController = new MainController(
                mainView,
                authService,
                configuratoreController,
                fruitoreController);

        try {
            stateService.controllaScadenze();
            startMidnightScheduler(stateService);
            mainController.run();
        } finally {
            stopMidnightScheduler();
        }
    }

    /**
     * Avvia un thread daemon che esegue {@link StateTransitionService#controllaScadenze()}
     * ogni notte alla mezzanotte.
     */
    private void startMidnightScheduler(StateTransitionService stateService) {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "state-transition-midnight");
            thread.setDaemon(true);
            return thread;
        };

        midnightScheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduleNextMidnightCheck(stateService);
    }

    private void stopMidnightScheduler() {
        if (midnightScheduler != null) {
            midnightScheduler.shutdownNow();
        }
    }

    /**
     * Calcola i millisecondi mancanti alla mezzanotte successiva.
     */
    private long millisUntilNextMidnight() {
        ZonedDateTime now = ZonedDateTime.now(AppConstants.clock);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, nextMidnight).toMillis();
    }

    private void scheduleNextMidnightCheck(StateTransitionService stateService) {
        if (midnightScheduler == null || midnightScheduler.isShutdown()) {
            return;
        }
        long delay = millisUntilNextMidnight();
        midnightScheduler.schedule(() -> {
            stateService.controllaScadenze();
            scheduleNextMidnightCheck(stateService);
        }, delay, TimeUnit.MILLISECONDS);
    }
}
