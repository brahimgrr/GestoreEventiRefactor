package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.notifica.ArchivioNotifiche;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;

import java.nio.file.Path;

/**
 * Implementazione JSON su file di {@link ISpazioPersonaleRepository}.
 */
public final class FileSpazioPersonaleRepository extends AbstractFileRepository<ArchivioNotifiche> implements ISpazioPersonaleRepository {

    public FileSpazioPersonaleRepository(Path path) {
        super(path, ArchivioNotifiche.class, ArchivioNotifiche::new);
    }

    @Override
    public ArchivioNotifiche load() {
        return super.load();
    }


    @Override
    public void save(ArchivioNotifiche archivio) {
        super.save(archivio);
    }
}
