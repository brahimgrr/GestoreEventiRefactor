package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.dto.ArchivioNotificheDTO;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ISpazioPersonaleRepository}.
 */
public final class FileSpazioPersonaleRepository extends AbstractFileRepository<ArchivioNotificheDTO> implements ISpazioPersonaleRepository {

    public FileSpazioPersonaleRepository(Path path) {
        super(path, ArchivioNotificheDTO.class, ArchivioNotificheDTO::new);
    }

    @Override
    public ArchivioNotificheDTO load() {
        return super.load();
    }


    @Override
    public void save(ArchivioNotificheDTO archivio) {
        super.save(archivio);
    }
}
