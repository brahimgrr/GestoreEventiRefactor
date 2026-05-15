package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;

import java.util.List;

public record CategoriaDocument(String nome, List<CampoDocument> campiSpecifici) {
    public CategoriaDocument {
        campiSpecifici = campiSpecifici == null ? List.of() : List.copyOf(campiSpecifici);
    }

    public static CategoriaDocument fromDomain(Categoria categoria) {
        return new CategoriaDocument(
                categoria.getNome(),
                categoria.getCampiSpecifici().stream()
                        .map(CampoDocument::fromDomain)
                        .toList());
    }

    public Categoria toDomain() {
        List<Campo> campi = campiSpecifici.stream()
                .map(CampoDocument::toDomain)
                .toList();
        return Categoria.rehydrate(nome, campi);
    }
}
