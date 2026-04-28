package it.unibs.ingsoft.testsupport;

import it.unibs.ingsoft.domain.ArchivioNotifiche;
import it.unibs.ingsoft.domain.SpazioPersonale;
import it.unibs.ingsoft.persistence.api.ISpazioPersonaleRepository;

public final class InMemorySpazioPersonaleRepository implements ISpazioPersonaleRepository {
    private final ArchivioNotifiche archivio;
    private int saveCount;

    public InMemorySpazioPersonaleRepository() {
        this(new ArchivioNotifiche());
    }

    public InMemorySpazioPersonaleRepository(ArchivioNotifiche archivio) {
        this.archivio = archivio;
    }

    @Override
    public SpazioPersonale get(String username) {
        return archivio.getSpazioDi(username);
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int saveCount() {
        return saveCount;
    }

    public ArchivioNotifiche archivio() {
        return archivio;
    }
}
