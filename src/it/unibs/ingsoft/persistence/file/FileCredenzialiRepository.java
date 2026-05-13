package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ICredenzialiRepository}.
 */
public final class FileCredenzialiRepository
        extends AbstractFileRepository<CredenzialiDTO>
        implements ICredenzialiRepository {

    public FileCredenzialiRepository(Path path) {
        super(path, CredenzialiDTO.class, CredenzialiDTO::new);
    }

    @Override
    public CredenzialiDTO load() {
        return super.load();
    }

    @Override
    public void save(CredenzialiDTO credenziali) {
        super.save(credenziali);
    }
}
