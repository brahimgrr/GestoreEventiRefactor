package it.unibs.ingsoft.testsupport;

import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

public final class InMemoryBachecaRepository implements IBachecaRepository {
    private final Bacheca bacheca;
    private int saveCount;

    public InMemoryBachecaRepository() {
        this(new Bacheca());
    }

    public InMemoryBachecaRepository(Bacheca bacheca) {
        this.bacheca = bacheca;
    }

    @Override
    public Bacheca get() {
        return bacheca;
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int saveCount() {
        return saveCount;
    }
}
