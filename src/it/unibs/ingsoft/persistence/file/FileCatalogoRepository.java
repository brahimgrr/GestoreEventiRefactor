package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.Catalogo;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ICatalogoRepository}.
 */
public final class FileCatalogoRepository
        extends AbstractFileRepository<Catalogo>
        implements ICatalogoRepository {

    private Catalogo cached;

    public FileCatalogoRepository(Path path) {
        super(path, Catalogo.class, Catalogo::new);
    }

    @Override
    public Catalogo get() {
        if (cached == null) {
            cached = load();
        }
        return cached;
    }

    @Override
    public void save() {
        if (cached != null) {
            super.save(cached);
        }
    }
}