package it.unibs.ingsoft.integration;

import it.unibs.ingsoft.application.NotificationService;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.persistence.impl.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.presentation.controller.SpazioPersonaleController;
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
        NotificationService service = new NotificationService(new FileSpazioPersonaleRepository(notificheFile));
        service.inviaNotifica("mario", new Notifica("La proposta e' stata confermata"));
        ScriptedAppView view = new ScriptedAppView()
                .integers(1)
                .booleans(true);
        SpazioPersonaleController controller =
                new SpazioPersonaleController(new Fruitore("mario"), view, service);

        controller.run();

        NotificationService reloaded = new NotificationService(new FileSpazioPersonaleRepository(notificheFile));
        assertTrue(reloaded.getNotifiche("mario").isEmpty());
    }
}
