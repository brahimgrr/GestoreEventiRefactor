package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link IBachecaRepository}.
 */
public final class FileBachecaRepository
        extends AbstractFileRepository<BachecaDTO>
        implements IBachecaRepository {

    public FileBachecaRepository(Path path) {
        super(path, BachecaDTO.class, BachecaDTO::new);
    }

    @Override
    public BachecaDTO load() {
        return super.load();
    }

    @Override
    public void save(BachecaDTO bacheca) {
        super.save(bacheca);
    }
}
