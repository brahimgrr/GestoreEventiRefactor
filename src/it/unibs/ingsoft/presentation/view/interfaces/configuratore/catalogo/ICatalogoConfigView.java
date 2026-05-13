package it.unibs.ingsoft.presentation.view.interfaces.configuratore.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICatalogoConfigView {
    Optional<List<CampoBaseExtraRequest>> acquisisciCampiBaseExtra(List<Campo> campiPredefiniti);

    void mostraPrimaConfigurazioneRichiesta();

    void mostraCatalogo(List<Campo> base, List<Campo> comuni, List<Categoria> categorie);

    void mostraEsitoCatalogo(CatalogoOperationResult result);
}
