package it.unibs.ingsoft.presentation.view.interfaces.configuratore.categoria;

import it.unibs.ingsoft.domain.model.catalogo.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICategoriaConfigView {
    CategoryAction scegliAzioneCategorie(List<Categoria> categorie);

    Optional<String> acquisisciNomeCategoria();

    Optional<Categoria> selezionaCategoriaDaRimuovere(List<Categoria> categorie);

    Optional<Categoria> selezionaCategoriaPerCampiSpecifici(List<Categoria> categorie);

    boolean confermaRimozioneCategoria(Categoria categoria);

    enum CategoryAction {
        CREA,
        RIMUOVI,
        CAMPI_SPECIFICI,
        TORNA
    }
}
