package it.unibs.ingsoft.composition;

import it.unibs.ingsoft.application.IscrizioneService;
import it.unibs.ingsoft.application.NotificationService;
import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.application.StateTransitionService;
import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.api.ISpazioPersonaleRepository;
import it.unibs.ingsoft.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.persistence.impl.FileSpazioPersonaleRepository;
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
    private static final Path DATA_CATALOGO = Path.of("data/v5", "catalogo.json");
    private static final Path DATA_UTENTI = Path.of("data/v5", "utenti.json");
    private static final Path DATA_PROPOSTE = Path.of("data/v5", "proposte.json");
    private static final Path DATA_NOTIFICHE = Path.of("data/v5", "notifiche.json");
    private ScheduledExecutorService midnightScheduler;

    public void start() {
        ICatalogoRepository catalogoRepo = new FileCatalogoRepository(DATA_CATALOGO);
        ICredenzialiRepository credenzialiRepo = new FileCredenzialiRepository(DATA_UTENTI);
        IBachecaRepository propostaRepo = new FileBachecaRepository(DATA_PROPOSTE);
        ISpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(DATA_NOTIFICHE);

        AuthenticationService authService = new AuthenticationService(credenzialiRepo);
        CatalogoService catalogoService = new CatalogoService(catalogoRepo);
        PropostaService propostaService = new PropostaService(propostaRepo);
        NotificationService notifService = new NotificationService(spazioRepo);
        StateTransitionService stateService = new StateTransitionService(propostaRepo, notifService);
        IscrizioneService iscrizioneService = new IscrizioneService(propostaRepo, stateService);

        BatchImportService batchImportService = new BatchImportService(catalogoService, propostaService);

        IAppView ui = new ConsoleUI(new Scanner(System.in));
        IMainView mainView = new MainCliView(ui);
        IConfiguratoreView configuratoreView = new ConfiguratoreCliView(ui);
        IFruitoreView fruitoreView = new FruitoreCliView(ui);

        ConfiguratoreController configuratoreController = new ConfiguratoreController(
                configuratoreView,
                catalogoService,
                propostaService,
                stateService,
                batchImportService);

        FruitoreController fruitoreController = new FruitoreController(
                fruitoreView,
                propostaService,
                iscrizioneService,
                notifService);

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
