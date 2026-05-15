package it.unibs.ingsoft.presentation.view.cli.fruitore.notifica;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.notifica.ISpazioPersonaleView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public final class SpazioPersonaleView implements ISpazioPersonaleView {
    private static final DateTimeFormatter NOTIFICA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final IAppView ui;

    public SpazioPersonaleView(IAppView ui) {
        this.ui = ui;
    }

    @Override
    public Optional<Notifica> selezionaNotificaDaEliminare(Fruitore fruitore, List<Notifica> notifiche) {
        ui.header("SPAZIO PERSONALE DI " + fruitore.getUsername());

        if (notifiche.isEmpty()) {
            ui.stampa("Nessuna notifica presente.");
            ui.newLine();
            ui.pausa();
            return Optional.empty();
        }

        for (int i = 0; i < notifiche.size(); i++) {
            Notifica notifica = notifiche.get(i);
            ui.stampa((i + 1) + ". [" + notifica.dataCreazione().format(NOTIFICA_FMT) + "] "
                    + NotificaMessageMapper.message(notifica));
        }
        ui.newLine();

        ui.stampa("Digita il numero di una notifica per eliminarla (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, notifiche.size());
        return choice == 0 ? Optional.empty() : Optional.of(notifiche.get(choice - 1));
    }

    @Override
    public boolean confermaEliminazioneNotifica(Notifica notifica) {
        try {
            return ui.acquisisciSiNo("Confermi l'eliminazione di questa notifica?");
        } catch (OperationCancelledException e) {
            return false;
        }
    }

    @Override
    public void mostraNotificaEliminata(Notifica notifica) {
        ui.stampaSuccesso("Notifica eliminata.");
        ui.newLine();
    }
}
