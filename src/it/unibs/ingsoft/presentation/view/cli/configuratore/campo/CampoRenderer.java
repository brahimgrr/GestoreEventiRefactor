package it.unibs.ingsoft.presentation.view.cli.configuratore.campo;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;

import java.util.List;

public final class CampoRenderer {
    private final IAppView ui;

    public CampoRenderer(IAppView ui) {
        this.ui = ui;
    }

    public void stampaCampi(List<Campo> campi) {
        if (campi.isEmpty()) {
            ui.stampa("    (nessun campo)");
            return;
        }
        for (Campo c : campi)
            ui.stampa("  - " + formatCampo(c));
    }

    public String formatCampo(Campo campo) {
        return campo.getNome() + " [" + campo.getTipoDato() + "]"
                + (campo.isObbligatorio() ? "  (obbligatorio)" : "");
    }

    public TipoDato acquisisciTipoDato(String prompt) {
        TipoDato[] valori = TipoDato.values();
        ui.stampa(prompt);
        for (int i = 0; i < valori.length; i++)
            ui.stampa("  " + (i + 1) + ") " + valori[i]);
        ui.newLine();

        int choice = ui.acquisisciIntero("Scelta: ", 1, valori.length);
        return valori[choice - 1];
    }
}
