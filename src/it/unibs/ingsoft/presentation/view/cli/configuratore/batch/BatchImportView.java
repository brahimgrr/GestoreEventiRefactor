package it.unibs.ingsoft.presentation.view.cli.configuratore.batch;

import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.presentation.view.cli.common.error.FailureMessageRegistry;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.batch.IBatchImportView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.nio.file.Path;
import java.util.Optional;

public final class BatchImportView implements IBatchImportView {
    private final IAppView ui;
    private final FailureMessageRegistry messages;

    public BatchImportView(IAppView ui) {
        this(ui, FailureMessageRegistry.cliDefault());
    }

    public BatchImportView(IAppView ui, FailureMessageRegistry messages) {
        this.ui = ui;
        this.messages = messages;
    }

    @Override
    public Optional<Path> acquisisciPercorsoImportazione() {
        ui.header("IMPORTAZIONE BATCH");
        ui.stampaInfo("Importa categorie, campi comuni e proposte da un file JSON.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        try {
            String percorso = ui.acquisisciStringa("Percorso del file JSON: ");
            if (percorso == null || percorso.isBlank()) {
                ui.stampaErrore("Percorso non valido.");
                ui.pausaConSpaziatura();
                return Optional.empty();
            }

            ui.stampa("Importazione in corso...");
            ui.newLine();
            return Optional.of(Path.of(percorso.trim()));
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public void mostraRisultatoImportazione(ImportResult result) {
        ui.stampaSezione("RISULTATO IMPORTAZIONE");
        ui.stampa("  Campi comuni importati: " + result.getCampiComuniImportati());
        ui.stampa("  Categorie importate:    " + result.getCategorieImportate());
        ui.stampa("  Proposte importate:     " + result.getProposteImportate());
        ui.newLine();

        if (result.hasErrors()) {
            ui.stampaAvviso("Errori riscontrati (" + result.getErrori().size() + "):");
            for (var errore : result.getErrori()) {
                ui.stampaErrore(messages.message(errore.failure()));
            }
            ui.newLine();
        }

        if (result.totaleImportati() == 0 && !result.hasErrors()) {
            ui.stampaInfo("Il file non contiene dati da importare.");
        } else if (result.totaleImportati() > 0) {
            ui.stampaSuccesso("Importazione completata: " + result.totaleImportati() + " elementi importati.");
            if (result.getProposteImportate() > 0) {
                ui.stampaInfo("Le proposte valide possono essere pubblicate dal menu 'Pubblicare una proposta di iniziativa'.");
            }
        }
        ui.pausaConSpaziatura();
    }

    private void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
