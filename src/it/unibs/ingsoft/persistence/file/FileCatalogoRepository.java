package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.catalogo.Catalogo;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ICatalogoRepository}.
 */
public final class FileCatalogoRepository
        extends AbstractFileRepository<Catalogo>
        implements ICatalogoRepository {

    public FileCatalogoRepository(Path path) {
        super(path, Catalogo.class, Catalogo::new);
    }

    @Override
    public Catalogo load() {
        return super.load();
    }

    @Override
    public void save(Catalogo catalogo) {
        super.save(catalogo);
    }
}
