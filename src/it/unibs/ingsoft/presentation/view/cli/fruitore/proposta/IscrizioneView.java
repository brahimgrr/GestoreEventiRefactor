package it.unibs.ingsoft.presentation.view.cli.fruitore.proposta;

import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.cli.common.error.FailureMessageRegistry;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta.IIscrizioneView;
import it.unibs.ingsoft.shared.error.Failure;

import java.util.List;
import java.util.Optional;

public final class IscrizioneView implements IIscrizioneView {
    private final IAppView ui;
    private final PropostaRenderer propostaRenderer;
    private final FailureMessageRegistry messages;

    public IscrizioneView(IAppView ui, PropostaRenderer propostaRenderer) {
        this(ui, propostaRenderer, FailureMessageRegistry.cliDefault());
    }

    public IscrizioneView(IAppView ui, PropostaRenderer propostaRenderer, FailureMessageRegistry messages) {
        this.ui = ui;
        this.propostaRenderer = propostaRenderer;
        this.messages = messages;
    }

    @Override
    public boolean confermaIscrizione(Proposta proposta) {
        ui.header("DETTAGLI PROPOSTA");
        propostaRenderer.mostraRiepilogoProposta(proposta);
        ui.newLine();
        try {
            boolean confermata = ui.acquisisciSiNo("Vuoi iscriverti a questa proposta?");
            if (!confermata) {
                ui.pausaConSpaziatura();
            }
            return confermata;
        } catch (OperationCancelledException e) {
            ui.pausaConSpaziatura();
            return false;
        }
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte) {
        if (proposte.isEmpty()) {
            ui.stampa("Non sei iscritto a nessuna proposta aperta.");
            ui.pausaConSpaziatura();
            return Optional.empty();
        }

        ui.header("DISDICI ISCRIZIONE");
        for (int i = 0; i < proposte.size(); i++) {
            Proposta proposta = proposte.get(i);
            ui.stampa(String.format(" %d) %s (Scadenza: %s)",
                    i + 1,
                    propostaRenderer.titolo(proposta, "Senza Titolo"),
                    proposta.getTermineIscrizione()));
        }
        ui.newLine();

        ui.stampa("Seleziona la proposta da cui disdire l'iscrizione (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, proposte.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposte.get(choice - 1));
    }

    @Override
    public boolean confermaDisiscrizione(Proposta proposta) {
        propostaRenderer.mostraRiepilogoProposta(proposta);
        ui.newLine();
        try {
            boolean confermata = ui.acquisisciSiNo("Vuoi davvero disdire l'iscrizione a questa proposta?");
            if (!confermata) {
                ui.pausaConSpaziatura();
            }
            return confermata;
        } catch (OperationCancelledException e) {
            ui.pausaConSpaziatura();
            return false;
        }
    }

    @Override
    public void mostraIscrizioneEffettuata(Proposta proposta) {
        ui.stampaSuccesso("Iscrizione effettuata con successo!");
        ui.pausaConSpaziatura();
    }

    @Override
    public void mostraDisiscrizioneEffettuata(Proposta proposta) {
        ui.stampaSuccesso("Iscrizione disdetta con successo.");
        ui.pausaConSpaziatura();
    }

    @Override
    public void mostraErrore(Failure failure) {
        ui.stampaErrore(messages.message(failure));
        ui.pausaConSpaziatura();
    }

}
