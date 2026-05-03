package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Catalogo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;

import java.util.List;
import java.util.Objects;

/**
 * Gestisce le categorie del catalogo.
 */
public final class CategoriaCatalogoService {
    private final ICatalogoRepository repo;

    public CategoriaCatalogoService(ICatalogoRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    private Catalogo catalogo() {
        return repo.get();
    }

    public Categoria createCategoria(String nome) {
        Categoria categoria = catalogo().addCategoria(nome);
        repo.save();
        return categoria;
    }

    public boolean removeCategoria(String nome) {
        boolean changed = catalogo().removeCategoria(nome);
        if (changed) repo.save();
        return changed;
    }

    public CatalogoOperationResult rimuoviCategoria(String nome) {
        return removeCategoria(nome)
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    public List<Categoria> getCategorie() {
        return catalogo().getCategorie();
    }
}
