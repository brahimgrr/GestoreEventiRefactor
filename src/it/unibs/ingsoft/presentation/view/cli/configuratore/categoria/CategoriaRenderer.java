package it.unibs.ingsoft.presentation.view.cli.configuratore.categoria;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.presentation.view.cli.configuratore.campo.CampoRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.util.List;
import java.util.OptionalInt;

public final class CategoriaRenderer {
    private final IAppView ui;
    private final CampoRenderer campoRenderer;

    public CategoriaRenderer(IAppView ui, CampoRenderer campoRenderer) {
        this.ui = ui;
        this.campoRenderer = campoRenderer;
    }

    public void stampaCategorie(List<Categoria> categorie) {
        if (categorie.isEmpty()) {
            ui.stampa("  (nessuna categoria)");
            return;
        }
        for (Categoria cat : categorie) {
            ui.stampa("  - " + formatCategoria(cat));
            for (Campo c : cat.getCampiSpecifici())
                ui.stampa("      - " + campoRenderer.formatCampo(c));
        }
    }

    public OptionalInt selezionaCategoria(List<Categoria> categorie) {
        if (categorie.isEmpty()) {
            ui.stampaAvviso("Nessuna categoria disponibile.");
            return OptionalInt.empty();
        }
        for (int i = 0; i < categorie.size(); i++)
            ui.stampa("  " + (i + 1) + ". " + formatCategoria(categorie.get(i)));
        ui.stampa("  0. Annulla");

        try {
            int scelta = ui.acquisisciIntero("Scelta: ", 0, categorie.size());
            if (scelta == 0) return OptionalInt.empty();
            return OptionalInt.of(scelta - 1);
        } catch (OperationCancelledException e) {
            return OptionalInt.empty();
        }
    }

    public String formatCategoria(Categoria categoria) {
        return categoria.getNome();
    }
}
