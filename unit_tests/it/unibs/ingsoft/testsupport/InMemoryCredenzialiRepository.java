package it.unibs.ingsoft.testsupport;

import it.unibs.ingsoft.domain.Credenziali;
import it.unibs.ingsoft.persistence.api.ICredenzialiRepository;

public final class InMemoryCredenzialiRepository implements ICredenzialiRepository {
    private final Credenziali credenziali;
    private int saveCount;

    public InMemoryCredenzialiRepository() {
        this(new Credenziali());
    }

    public InMemoryCredenzialiRepository(Credenziali credenziali) {
        this.credenziali = credenziali;
    }

    @Override
    public Credenziali get() {
        return credenziali;
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int saveCount() {
        return saveCount;
    }
}
