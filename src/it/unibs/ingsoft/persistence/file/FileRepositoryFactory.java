package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;

import java.nio.file.Path;
import java.util.Objects;

public final class FileRepositoryFactory {
    private static final Path DEFAULT_DATA_DIR = Path.of("data/v5");

    private static FileRepositoryFactory instance;
    private final Path dataDir;

    private FileRepositoryFactory() {
        this(DEFAULT_DATA_DIR);
    }

    public FileRepositoryFactory(Path dataDir) {
        this.dataDir = Objects.requireNonNull(dataDir);
    }

    public static FileRepositoryFactory getInstance() {
        if (instance == null) {
            instance = new FileRepositoryFactory();
        }
        return instance;
    }

    public ICatalogoRepository createCatalogoRepository() {
        return new FileCatalogoRepository(dataDir.resolve("catalogo.json"));
    }

    public ICredenzialiRepository createCredenzialiRepository() {
        return new FileCredenzialiRepository(dataDir.resolve("utenti.json"));
    }

    public IBachecaRepository createBachecaRepository() {
        return new FileBachecaRepository(dataDir.resolve("proposte.json"));
    }

    public ISpazioPersonaleRepository createSpazioPersonaleRepository() {
        return new FileSpazioPersonaleRepository(dataDir.resolve("notifiche.json"));
    }
}
