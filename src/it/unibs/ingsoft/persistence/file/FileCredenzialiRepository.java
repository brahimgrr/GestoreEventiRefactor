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
    private Credenziali cached;

    public FileCredenzialiRepository(Path path) {
        super(path, Credenziali.class, Credenziali::new);
    }

    @Override
    public Credenziali get() {
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
