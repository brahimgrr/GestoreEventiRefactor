package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.Credenziali;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ICredenzialiRepository}.
 */
public final class FileCredenzialiRepository
        extends AbstractFileRepository<Credenziali>
        implements ICredenzialiRepository {

    public FileCredenzialiRepository(Path path) {
        super(path, Credenziali.class, Credenziali::new);
    }

    @Override
    public Credenziali load() {
        return super.load();
    }

    @Override
    public void save(Credenziali credenziali) {
        super.save(credenziali);
    }
}
