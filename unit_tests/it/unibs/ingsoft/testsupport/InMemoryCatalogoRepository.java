package it.unibs.ingsoft.testsupport;

import it.unibs.ingsoft.domain.Catalogo;
import it.unibs.ingsoft.persistence.api.ICatalogoRepository;

public final class InMemoryCatalogoRepository implements ICatalogoRepository {
    private final Catalogo catalogo;
    private int saveCount;

    public InMemoryCatalogoRepository() {
        this(new Catalogo());
    }

    public InMemoryCatalogoRepository(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public Catalogo get() {
        return catalogo;
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int saveCount() {
        return saveCount;
    }
}
