package it.unibs.ingsoft.presentation.view.cli.configuratore.categoria;

import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.categoria.ICategoriaConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.util.List;
import java.util.Optional;

public final class CategoriaConfigView implements ICategoriaConfigView {
    private static final String[] MENU_CATEGORIE = {
            "Crea categoria",
            "Rimuovi categoria",
            "Gestisci campi specifici di una categoria"
    };

    private final IAppView ui;
    private final CategoriaRenderer categoriaRenderer;

    public CategoriaConfigView(IAppView ui, CategoriaRenderer categoriaRenderer) {
        this.ui = ui;
        this.categoriaRenderer = categoriaRenderer;
    }

    @Override
    public CategoryAction scegliAzioneCategorie(List<Categoria> categorie) {
        ui.header("CATEGORIE");
        categoriaRenderer.stampaCategorie(categorie);
        ui.newLine();
        ui.stampaMenu("", MENU_CATEGORIE, "Torna");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CATEGORIE.length);
        ui.newLine();
        return choice == 0 ? CategoryAction.TORNA : CategoryAction.values()[choice - 1];
    }

    @Override
    public Optional<String> acquisisciNomeCategoria() {
        try {
            ui.stampaInfo(IAppView.HINT_ANNULLA);
            return Optional.of(ui.acquisisciStringaConValidazione(
                    "Nome nuova categoria: ",
                    n -> !n.isBlank(),
                    "Il nome non puo essere vuoto."
            ));
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Categoria> selezionaCategoriaDaRimuovere(List<Categoria> categorie) {
        return ui.selezionaElemento("Seleziona categoria da rimuovere:", categorie, categoriaRenderer::formatCategoria);
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerCampiSpecifici(List<Categoria> categorie) {
        return ui.selezionaElemento("Seleziona categoria:", categorie, categoriaRenderer::formatCategoria);
    }

    @Override
    public boolean confermaRimozioneCategoria(Categoria categoria) {
        try {
            return ui.acquisisciSiNo("Rimuovere '" + categoria.getNome() + "' e tutti i suoi campi specifici?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    private void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
