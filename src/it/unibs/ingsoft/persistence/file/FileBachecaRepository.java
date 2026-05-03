package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link IBachecaRepository}.
 */
public final class FileBachecaRepository
        extends AbstractFileRepository<Bacheca>
        implements IBachecaRepository {
    private Bacheca cached;

    public FileBachecaRepository(Path path) {
        super(path, Bacheca.class, Bacheca::new);
    }

    @Override
    public Bacheca get() {
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
