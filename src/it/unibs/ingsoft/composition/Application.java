package it.unibs.ingsoft.composition;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.FruitoreService;
import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.catalogo.CampoCatalogoService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.catalogo.CategoriaCatalogoService;
import it.unibs.ingsoft.application.proposta.PropostaLifecycleService;
import it.unibs.ingsoft.application.proposta.PropostaPublicationService;
import it.unibs.ingsoft.application.proposta.PropostaCommandLock;
import it.unibs.ingsoft.application.proposta.PropostaQueryService;
import it.unibs.ingsoft.application.proposta.PropostaValidationService;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.catalogo.CampoFactory;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
import it.unibs.ingsoft.domain.utente.UtenteFactory;
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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public final class Application {
    private ScheduledExecutorService midnightScheduler;

    public void start() {
        CampoFactory campoFactory = CampoFactory.getInstance();
        NotificaFactory notificaFactory = NotificaFactory.getInstance();
        UtenteFactory utenteFactory = UtenteFactory.getInstance();

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

        PropostaValidationService propostaValidationService = new PropostaValidationService();
        PropostaQueryService propostaQueryService = new PropostaQueryService(propostaRepo);
        NotificationService notifService = new NotificationService(spazioRepo);
        PropostaCommandLock propostaCommandLock = new PropostaCommandLock();
        PropostaPublicationService propostaPublicationService =
                new PropostaPublicationService(
                        propostaRepo,
                        PropostaIdentityPolicy.DEFAULT,
                        propostaCommandLock);
        PropostaLifecycleService propostaLifecycleService =
                new PropostaLifecycleService(propostaRepo, notifService, notificaFactory, propostaCommandLock);
        PropostaService propostaService = new PropostaService(
                propostaValidationService,
                propostaPublicationService,
                propostaLifecycleService,
                propostaQueryService);

        AuthenticationService authService = new AuthenticationService(credenzialiRepo, utenteFactory);

        BatchImportService batchImportService = new BatchImportService(catalogoService, propostaService);
        ConfiguratoreService configuratoreService = new ConfiguratoreService(
                catalogoService,
                propostaService,
                batchImportService);
        FruitoreService fruitoreService = new FruitoreService(
                propostaService,
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
            propostaService.controllaScadenze();
            startMidnightScheduler(propostaService);
            mainController.run();
        } finally {
            stopMidnightScheduler();
        }
    }

    private void startMidnightScheduler(PropostaService propostaService) {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "state-transition-midnight");
            thread.setDaemon(true);
            return thread;
        };

        midnightScheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduleNextMidnightCheck(propostaService);
    }

    private void stopMidnightScheduler() {
        if (midnightScheduler != null) {
            midnightScheduler.shutdownNow();
        }
    }

    private long millisUntilNextMidnight() {
        ZonedDateTime now = ZonedDateTime.now(AppConstants.clock);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, nextMidnight).toMillis();
    }

    private void scheduleNextMidnightCheck(PropostaService propostaService) {
        if (midnightScheduler == null || midnightScheduler.isShutdown()) {
            return;
        }
        long delay = millisUntilNextMidnight();
        midnightScheduler.schedule(() -> {
            propostaService.controllaScadenze();
            scheduleNextMidnightCheck(propostaService);
        }, delay, TimeUnit.MILLISECONDS);
    }
}
