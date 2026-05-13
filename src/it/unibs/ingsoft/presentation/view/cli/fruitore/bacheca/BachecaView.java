package it.unibs.ingsoft.presentation.view.cli.fruitore.bacheca;

import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca.IBachecaView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BachecaView implements IBachecaView {
    private final IAppView ui;
    private final PropostaRenderer propostaRenderer;

    public BachecaView(IAppView ui, PropostaRenderer propostaRenderer) {
        this.ui = ui;
        this.propostaRenderer = propostaRenderer;
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca) {
        if (bacheca.isEmpty()) {
            ui.stampa("La bacheca e vuota. Nessuna proposta aperta al momento.");
            ui.pausaConSpaziatura();
            return Optional.empty();
        }

        ui.header("BACHECA PROPOSTE");
        List<Proposta> proposteSelezionabili = new ArrayList<>();
        int indice = 1;

        for (Map.Entry<String, List<Proposta>> entry : bacheca.entrySet()) {
            ui.stampaSezione("Categoria: " + entry.getKey());
            for (Proposta proposta : entry.getValue()) {
                proposteSelezionabili.add(proposta);
                ui.stampa(String.format(" %d) %s (Scadenza: %s, %s)",
                        indice++,
                        propostaRenderer.titolo(proposta, "Senza Titolo"),
                        proposta.getTermineIscrizione(),
                        propostaRenderer.postiLiberi(proposta)));
            }
            ui.newLine();
        }

        ui.stampa("Digita il numero della proposta per i dettagli o per iscriverti (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, proposteSelezionabili.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposteSelezionabili.get(choice - 1));
    }

}
