package it.unibs.ingsoft.persistence.impl;

import it.unibs.ingsoft.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.api.ISpazioPersonaleRepository;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Centralizza la costruzione dei repository persistenti su file.
 */
public final class FileRepositoryFactory {
    private final Path catalogoPath;
    private final Path credenzialiPath;
    private final Path bachecaPath;
    private final Path spazioPersonalePath;

    public FileRepositoryFactory(Path catalogoPath,
                                 Path credenzialiPath,
                                 Path bachecaPath,
                                 Path spazioPersonalePath) {
        this.catalogoPath = Objects.requireNonNull(catalogoPath, "Il path del catalogo non puo essere null.");
        this.credenzialiPath = Objects.requireNonNull(credenzialiPath, "Il path delle credenziali non puo essere null.");
        this.bachecaPath = Objects.requireNonNull(bachecaPath, "Il path della bacheca non puo essere null.");
        this.spazioPersonalePath = Objects.requireNonNull(spazioPersonalePath, "Il path dello spazio personale non puo essere null.");
    }

    public ICatalogoRepository createCatalogoRepository() {
        return new FileCatalogoRepository(catalogoPath);
    }

    public ICredenzialiRepository createCredenzialiRepository() {
        return new FileCredenzialiRepository(credenzialiPath);
    }

    public IBachecaRepository createBachecaRepository() {
        return new FileBachecaRepository(bachecaPath);
    }

    public ISpazioPersonaleRepository createSpazioPersonaleRepository() {
        return new FileSpazioPersonaleRepository(spazioPersonalePath);
    }
}
