package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.repository.CatalogoRepository;
import it.unibs.ingsoft.domain.model.catalogo.Catalogo;
import it.unibs.ingsoft.persistence.file.document.CatalogoDocument;

import java.nio.file.Path;
import java.util.function.Function;

public final class FileCatalogoRepository
        implements CatalogoRepository {
    private final Store store;

    public FileCatalogoRepository(Path path) {
        this.store = new Store(path);
    }

    @Override
    public Catalogo load() {
        return store.load().toDomain();
    }

    @Override
    public void save(Catalogo catalogo) {
        store.save(CatalogoDocument.fromDomain(catalogo));
    }

    @Override
    public <R> R update(Function<Catalogo, R> operation) {
        Catalogo catalogo = load();
        R result = operation.apply(catalogo);
        save(catalogo);
        return result;
    }

    private static final class Store extends AbstractFileRepository<CatalogoDocument> {
        private Store(Path path) {
            super(path, CatalogoDocument.class, CatalogoDocument::empty);
        }
    }
}
