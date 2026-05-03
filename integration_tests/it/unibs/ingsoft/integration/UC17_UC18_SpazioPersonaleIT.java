package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.bacheca.IscrizioneService;
import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.bacheca.StateTransitionService;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.persistence.file.FileBachecaRepository;
import it.unibs.ingsoft.persistence.file.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.presentation.controller.FruitoreController;
import it.unibs.ingsoft.presentation.view.cli.FruitoreCliView;
import it.unibs.ingsoft.testsupport.ScriptedAppView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UC17_UC18_SpazioPersonaleIT {
    @TempDir
    Path tempDir;

    @Test
    void UC17_UC18_spazioPersonaleShowsAndDeletesNotificationThroughControllerFlow() {
        Path notificheFile = tempDir.resolve("notifiche.json");
        Path proposteFile = tempDir.resolve("proposte.json");
        FileBachecaRepository bachecaRepo = new FileBachecaRepository(proposteFile);
        NotificationService service = new NotificationService(new FileSpazioPersonaleRepository(notificheFile));
        StateTransitionService stateService = new StateTransitionService(bachecaRepo, service);
        PropostaService propostaService = new PropostaService(bachecaRepo);
        IscrizioneService iscrizioneService = new IscrizioneService(bachecaRepo, stateService);
        service.inviaNotifica("mario", new Notifica("La proposta e' stata confermata"));
        ScriptedAppView view = new ScriptedAppView()
                .integers(3, 1, 0)
                .booleans(true);
        FruitoreController controller = new FruitoreController(
                new FruitoreCliView(view),
                propostaService,
                iscrizioneService,
                service);

        controller.run(new Fruitore("mario"));

        NotificationService reloaded = new NotificationService(new FileSpazioPersonaleRepository(notificheFile));
        assertTrue(reloaded.getNotifiche("mario").isEmpty());
    }
}
