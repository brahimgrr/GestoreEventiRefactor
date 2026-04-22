package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.NotificationService;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.presentation.view.contract.IAppView;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller per la visualizzazione e l'eliminazione delle notifiche nello spazio personale.
 */
public final class SpazioPersonaleController {

    private final Fruitore fruitore;
    private final IAppView ui;
    private final NotificationService notificationService;

    public SpazioPersonaleController(Fruitore fruitore, IAppView ui, NotificationService notificationService) {
        this.fruitore = fruitore;
        this.ui = ui;
        this.notificationService = notificationService;
    }

    public void run() {
        while (true) {
            ui.header("SPAZIO PERSONALE DI " + fruitore.getUsername());

            List<Notifica> notifiche = notificationService.getNotifiche(fruitore.getUsername());

            if (notifiche.isEmpty()) {
                ui.stampa("Nessuna notifica presente.");
                ui.newLine();
                ui.pausa();
                return;
            }

            for (int i = 0; i < notifiche.size(); i++) {
                Notifica n = notifiche.get(i);
                ui.stampa((i + 1) + ". [" + n.dataCreazione()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "] " + n.messaggio());
            }
            ui.newLine();

            ui.stampa("Digita il numero di una notifica per eliminarla (0 per tornare indietro).");
            int choice = ui.acquisisciIntero("Scelta: ", 0, notifiche.size());

            if (choice == 0) {
                return;
            }

            Notifica daEliminare = notifiche.get(choice - 1);
            if (ui.acquisisciSiNo("Confermi l'eliminazione di questa notifica?")) {
                notificationService.cancellaNotifica(fruitore.getUsername(), daEliminare);
                ui.stampaSuccesso("Notifica eliminata.");
                ui.newLine();
            }
        }
    }
}
