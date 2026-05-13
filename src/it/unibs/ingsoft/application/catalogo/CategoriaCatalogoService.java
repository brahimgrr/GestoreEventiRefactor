package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.persistence.dto.CatalogoDTO;
import it.unibs.ingsoft.domain.catalogo.Categoria;
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

    private CatalogoDTO catalogo() {
        return repo.load();
    }

    public Categoria createCategoria(String nome) {
        CatalogoDTO catalogo = repo.load();
        Categoria categoria = catalogo.addCategoria(nome);
        repo.save(catalogo);
        return categoria;
    }

    public boolean removeCategoria(String nome) {
        CatalogoDTO catalogo = repo.load();
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
