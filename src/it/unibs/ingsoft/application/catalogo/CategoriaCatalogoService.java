package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.model.catalogo.Catalogo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.repository.CatalogoRepository;

import java.util.List;
import java.util.Objects;

/**
 * Gestisce le categorie del catalogo.
 */
public final class CategoriaCatalogoService {
    private final CatalogoRepository repo;

    public CategoriaCatalogoService(CatalogoRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    private Catalogo catalogo() {
        return repo.load();
    }

    public Categoria createCategoria(String nome) {
        Catalogo catalogo = repo.load();
        Categoria categoria = catalogo.addCategoria(nome);
        repo.save(catalogo);
        return categoria;
    }

    public boolean removeCategoria(String nome) {
        Catalogo catalogo = repo.load();
        boolean changed = catalogo.removeCategoria(nome);
        if (changed) repo.save(catalogo);
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
