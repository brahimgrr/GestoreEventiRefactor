package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.CatalogoDTO;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ICatalogoRepository}.
 */
public final class FileCatalogoRepository
        extends AbstractFileRepository<CatalogoDTO>
        implements ICatalogoRepository {

    public FileCatalogoRepository(Path path) {
        super(path, CatalogoDTO.class, CatalogoDTO::new);
    }

    @Override
    public CatalogoDTO load() {
        return super.load();
    }

    @Override
    public void save(CatalogoDTO catalogo) {
        super.save(catalogo);
    }
}
