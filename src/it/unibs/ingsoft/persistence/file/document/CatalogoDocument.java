package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Catalogo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;

import java.util.List;

public record CatalogoDocument(
        List<CampoDocument> campiBase,
        boolean campiBaseFissati,
        List<CampoDocument> campiComuni,
        List<CategoriaDocument> categorie) {

    public CatalogoDocument {
        campiBase = campiBase == null ? List.of() : List.copyOf(campiBase);
        campiComuni = campiComuni == null ? List.of() : List.copyOf(campiComuni);
        categorie = categorie == null ? List.of() : List.copyOf(categorie);
    }

    public static CatalogoDocument empty() {
        return fromDomain(new Catalogo());
    }

    public static CatalogoDocument fromDomain(Catalogo catalogo) {
        return new CatalogoDocument(
                catalogo.getCampiBase().stream().map(CampoDocument::fromDomain).toList(),
                catalogo.isCampiBaseFissati(),
                catalogo.getCampiComuni().stream().map(CampoDocument::fromDomain).toList(),
                catalogo.getCategorie().stream().map(CategoriaDocument::fromDomain).toList());
    }

    public Catalogo toDomain() {
        List<Campo> base = campiBase.stream().map(CampoDocument::toDomain).toList();
        List<Campo> comuni = campiComuni.stream().map(CampoDocument::toDomain).toList();
        List<Categoria> categorieDomain = categorie.stream().map(CategoriaDocument::toDomain).toList();
        return Catalogo.rehydrate(base, campiBaseFissati, comuni, categorieDomain);
    }
}
