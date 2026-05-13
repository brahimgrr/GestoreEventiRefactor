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
import it.unibs.ingsoft.presentation.view.cli.fruitore.bacheca.BachecaView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.batch.BatchImportView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.campo.CampoConfigView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.campo.CampoRenderer;
import it.unibs.ingsoft.presentation.view.cli.configuratore.catalogo.CatalogoConfigView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.categoria.CategoriaConfigView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.categoria.CategoriaRenderer;
import it.unibs.ingsoft.presentation.view.cli.configuratore.error.ConfiguratoreFeedbackView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.menu.ConfiguratoreCliView;
import it.unibs.ingsoft.presentation.view.cli.common.ConsoleUI;
import it.unibs.ingsoft.presentation.view.cli.fruitore.menu.FruitoreCliView;
import it.unibs.ingsoft.presentation.view.cli.fruitore.proposta.IscrizioneView;
import it.unibs.ingsoft.presentation.view.cli.common.auth.MainCliView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.proposta.PropostaBrowsingView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.proposta.PropostaCreationView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.proposta.PropostaFormView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.proposta.PropostaLifecycleView;
import it.unibs.ingsoft.presentation.view.cli.configuratore.proposta.PropostaPublicationView;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.cli.fruitore.notifica.SpazioPersonaleView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca.IBachecaView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.batch.IBatchImportView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.campo.ICampoConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.catalogo.ICatalogoConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.categoria.ICategoriaConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.error.IConfiguratoreFeedbackView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu.IConfiguratoreView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta.IIscrizioneView;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.IMainView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaBrowsingView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaCreationView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaLifecycleView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaPublicationView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.notifica.ISpazioPersonaleView;

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
        CampoRenderer campoRenderer = new CampoRenderer(ui);
        CategoriaRenderer categoriaRenderer = new CategoriaRenderer(ui, campoRenderer);
        PropostaRenderer propostaRenderer = new PropostaRenderer(ui);
        PropostaFormView propostaFormView = new PropostaFormView(ui);
        IMainView mainView = new MainCliView(ui);
        IConfiguratoreView configuratoreView = new ConfiguratoreCliView(ui);
        ICatalogoConfigView catalogoConfigView = new CatalogoConfigView(ui, campoRenderer, categoriaRenderer);
        ICategoriaConfigView categoriaConfigView = new CategoriaConfigView(ui, categoriaRenderer);
        ICampoConfigView campoConfigView = new CampoConfigView(ui, campoRenderer);
        IPropostaCreationView propostaCreationView =
                new PropostaCreationView(ui, categoriaRenderer, propostaFormView, propostaRenderer);
        IPropostaPublicationView propostaPublicationView = new PropostaPublicationView(ui, propostaRenderer);
        IPropostaLifecycleView propostaLifecycleView = new PropostaLifecycleView(ui, propostaRenderer);
        IPropostaBrowsingView propostaBrowsingView = new PropostaBrowsingView(ui, propostaRenderer);
        IBatchImportView batchImportView = new BatchImportView(ui);
        IConfiguratoreFeedbackView configuratoreFeedbackView = new ConfiguratoreFeedbackView(ui);
        IFruitoreView fruitoreView = new FruitoreCliView(ui);
        IBachecaView bachecaView = new BachecaView(ui, propostaRenderer);
        IIscrizioneView iscrizioneView = new IscrizioneView(ui, propostaRenderer);
        ISpazioPersonaleView spazioPersonaleView = new SpazioPersonaleView(ui);

        ConfiguratoreController configuratoreController = new ConfiguratoreController(
                configuratoreView,
                catalogoConfigView,
                categoriaConfigView,
                campoConfigView,
                propostaCreationView,
                propostaPublicationView,
                propostaLifecycleView,
                propostaBrowsingView,
                batchImportView,
                configuratoreFeedbackView,
                configuratoreService);

        FruitoreController fruitoreController = new FruitoreController(
                fruitoreView,
                bachecaView,
                iscrizioneView,
                spazioPersonaleView,
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
