package it.unibs.ingsoft.presentation.view.cli.configuratore.proposta;

import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaPublicationView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.util.List;
import java.util.Optional;

public final class PropostaPublicationView implements IPropostaPublicationView {
    private final IAppView ui;
    private final PropostaRenderer propostaRenderer;

    public PropostaPublicationView(IAppView ui, PropostaRenderer propostaRenderer) {
        this.ui = ui;
        this.propostaRenderer = propostaRenderer;
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaPubblicare(List<Proposta> proposte) {
        ui.header("PUBBLICA PROPOSTA");
        if (proposte.isEmpty()) {
            ui.stampa("Nessuna proposta valida da pubblicare.");
            ui.newLine();
            ui.pausa();
            return Optional.empty();
        }

        ui.stampa("Proposte valide disponibili:");
        for (int i = 0; i < proposte.size(); i++) {
            Proposta p = proposte.get(i);
            String titolo = propostaRenderer.titolo(p, "(senza titolo)");
            String categoria = p.getCategoria().getNome();
            ui.stampa("  " + (i + 1) + ". " + titolo + "  [" + categoria + "]");
        }
        ui.stampa("  0. Torna");
        ui.newLine();

        int choice = ui.acquisisciIntero("Scelta: ", 0, proposte.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposte.get(choice - 1));
    }

    @Override
    public boolean confermaPubblicazione(Proposta proposta) {
        ui.newLine();
        propostaRenderer.mostraRiepilogoProposta(proposta);
        try {
            return ui.acquisisciSiNo("Vuoi pubblicare questa proposta in bacheca?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    @Override
    public void mostraPropostaPubblicata(Proposta proposta) {
        ui.stampaSuccesso("Proposta pubblicata in bacheca!");
        ui.newLine();
        ui.pausa();
    }

    private void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
