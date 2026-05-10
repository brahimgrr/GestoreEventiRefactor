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

    public FileBachecaRepository(Path path) {
        super(path, Bacheca.class, Bacheca::new);
    }

    @Override
    public Bacheca load() {
        return super.load();
    }

    @Override
    public void save(Bacheca bacheca) {
        super.save(bacheca);
    }
}
