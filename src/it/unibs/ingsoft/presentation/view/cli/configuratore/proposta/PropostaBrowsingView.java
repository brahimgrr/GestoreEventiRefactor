package it.unibs.ingsoft.presentation.view.cli.configuratore.proposta;

import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaBrowsingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PropostaBrowsingView implements IPropostaBrowsingView {
    private static final String[] MENU_DETTAGLI_PROPOSTA = {
            "Visualizza dettagli",
            "Visualizza aderenti",
            "Visualizza cronologia stati"
    };

    private final IAppView ui;
    private final PropostaRenderer propostaRenderer;

    public PropostaBrowsingView(IAppView ui, PropostaRenderer propostaRenderer) {
        this.ui = ui;
        this.propostaRenderer = propostaRenderer;
    }

    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        ui.header("BACHECA");
        propostaRenderer.mostraBacheca(bacheca);
        ui.newLine();
        ui.pausa();
    }

    @Override
    public void mostraArchivioProposte(Map<StatoProposta, List<Proposta>> archivio) {
        if (archivio.isEmpty()) {
            ui.stampa("  Archivio vuoto.");
            ui.pausaConSpaziatura();
            return;
        }

        List<StatoProposta> statiPresenti = new ArrayList<>(archivio.keySet());
        String[] menuStati = statiPresenti.stream()
                .map(s -> s.name() + " (" + archivio.get(s).size() + ")")
                .toArray(String[]::new);

        while (true) {
            ui.stampaMenu("ARCHIVIO PROPOSTE", menuStati, "Torna");
            int sceltaStato = ui.acquisisciIntero("Scelta: ", 0, statiPresenti.size());
            if (sceltaStato == 0) return;

            StatoProposta statoScelto = statiPresenti.get(sceltaStato - 1);
            List<Proposta> proposteStato = archivio.get(statoScelto);
            String[] menuProposte = proposteStato.stream()
                    .map(propostaRenderer::labelArchivio)
                    .toArray(String[]::new);

            while (true) {
                ui.stampaMenu("PROPOSTE IN STATO " + statoScelto.name(), menuProposte, "Torna");
                int sceltaProposta = ui.acquisisciIntero("Scelta: ", 0, proposteStato.size());
                if (sceltaProposta == 0) break;

                menuDettagliProposta(proposteStato.get(sceltaProposta - 1));
            }
        }
    }

    private void menuDettagliProposta(Proposta proposta) {
        String titolo = propostaRenderer.titolo(proposta, "?");
        while (true) {
            ui.stampaMenu("PROPOSTA - " + titolo, MENU_DETTAGLI_PROPOSTA, "Torna");
            int scelta = ui.acquisisciIntero("Scelta: ", 0, MENU_DETTAGLI_PROPOSTA.length);
            switch (scelta) {
                case 1:
                    propostaRenderer.mostraRiepilogoProposta(proposta);
                    ui.pausaConSpaziatura();
                    break;
                case 2:
                    propostaRenderer.mostraAderenti(proposta.getListaAderenti());
                    ui.pausaConSpaziatura();
                    break;
                case 3:
                    propostaRenderer.mostraCronologiaStati(proposta.getStateHistory());
                    ui.pausaConSpaziatura();
                    break;
                case 0:
                    return;
            }
        }
    }
}
