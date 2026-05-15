package it.unibs.ingsoft.presentation.view.cli.common.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.PropostaStateChange;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;

import java.util.List;
import java.util.Map;

public final class PropostaRenderer {
    private static final String SEPARATORE = "-".repeat(60);

    private final IAppView ui;

    public PropostaRenderer(IAppView ui) {
        this.ui = ui;
    }

    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        if (bacheca.isEmpty()) {
            ui.stampa("  La bacheca è vuota.");
            return;
        }
        bacheca.forEach((categoria, proposte) ->
        {
            ui.stampaSezione("Categoria: " + categoria);
            for (Proposta p : proposte) {
                for (String campo : p.getValoriCampi().keySet()) {
                    String valore = p.getValoriCampi().getOrDefault(campo, "");
                    if (!valore.isBlank())
                        ui.stampa("      " + campo + ": " + valore);
                }
                ui.newLine();
            }
        });
    }

    public void mostraRiepilogoProposta(Proposta proposta) {
        ui.newLine();
        ui.stampa(SEPARATORE);
        ui.stampa("  RIEPILOGO PROPOSTA — Categoria: " + proposta.getCategoria().getNome()
                + " | Stato: " + proposta.getStato());
        ui.stampa(SEPARATORE);
        for (String campo : proposta.getValoriCampi().keySet()) {
            String valore = proposta.getValoriCampi().getOrDefault(campo, "");
            ui.stampa("  " + campo + ": " + (valore.isBlank() ? "(non compilato)" : valore));
        }
        ui.stampa(SEPARATORE);
    }

    public void mostraAderenti(List<String> aderenti) {
        ui.newLine();
        ui.stampa(SEPARATORE);
        ui.stampa("  ADERENTI (" + aderenti.size() + ")");
        ui.stampa(SEPARATORE);
        if (aderenti.isEmpty())
            ui.stampa("  Nessun aderente.");
        else
            for (String a : aderenti)
                ui.stampa("  - " + a);
        ui.stampa(SEPARATORE);
    }

    public void mostraCronologiaStati(List<PropostaStateChange> history) {
        ui.newLine();
        ui.stampa(SEPARATORE);
        ui.stampa("  CRONOLOGIA STATI");
        ui.stampa(SEPARATORE);
        for (PropostaStateChange sc : history)
            ui.stampa("  " + sc.dataCambio().format(AppConstants.DATE_FMT)
                    + "  ->  " + sc.stato());
        ui.stampa(SEPARATORE);
    }

    public String titolo(Proposta proposta, String defaultValue) {
        return proposta.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, defaultValue);
    }

    public String postiLiberi(Proposta proposta) {
        return (proposta.getNumeroPartecipanti() - proposta.getListaAderenti().size()) + " posti liberi";
    }

    public String labelArchivio(Proposta proposta) {
        return titolo(proposta, "(senza titolo)") + " - " + proposta.getCategoria().getNome();
    }
}
