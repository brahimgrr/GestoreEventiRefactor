package it.unibs.ingsoft.presentation.view.cli.configuratore.proposta;

import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaLifecycleView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.util.List;
import java.util.Optional;

public final class PropostaLifecycleView implements IPropostaLifecycleView {
    private final IAppView ui;
    private final PropostaRenderer propostaRenderer;

    public PropostaLifecycleView(IAppView ui, PropostaRenderer propostaRenderer) {
        this.ui = ui;
        this.propostaRenderer = propostaRenderer;
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaRitirare(List<Proposta> proposte) {
        if (proposte.isEmpty()) {
            ui.stampa("Nessuna proposta aperta o confermata disponibile per il ritiro.");
            ui.pausaConSpaziatura();
            return Optional.empty();
        }

        ui.header("RITIRO PROPOSTA");
        stampaElencoProposteConStato(proposte);
        ui.stampa("Seleziona la proposta da ritirare (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, proposte.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposte.get(choice - 1));
    }

    @Override
    public boolean confermaRitiro(Proposta proposta) {
        propostaRenderer.mostraRiepilogoProposta(proposta);
        ui.newLine();
        ui.stampaAvviso("Il ritiro e una misura eccezionale, da adottare solo per cause di forza maggiore.");
        try {
            return ui.acquisisciSiNo("Vuoi davvero ritirare questa proposta?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    private void stampaElencoProposteConStato(List<Proposta> proposte) {
        int indice = 1;
        for (Proposta proposta : proposte) {
            String titolo = propostaRenderer.titolo(proposta, "Senza Titolo");
            String stato = proposta.getStato().name();
            ui.stampa(String.format(" %d) %s [%s] (Data evento: %s)",
                    indice++, titolo, stato, proposta.getDataEvento()));
        }
        ui.newLine();
    }

    private void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
