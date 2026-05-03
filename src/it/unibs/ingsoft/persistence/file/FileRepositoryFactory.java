package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;

import java.nio.file.Path;
import java.util.Objects;

public final class FileRepositoryFactory {
    private static final Path DATA_CATALOGO = Path.of("data/v5", "catalogo.json");
    private static final Path DATA_UTENTI = Path.of("data/v5", "utenti.json");
    private static final Path DATA_PROPOSTE = Path.of("data/v5", "proposte.json");
    private static final Path DATA_NOTIFICHE = Path.of("data/v5", "notifiche.json");

    private static FileRepositoryFactory instance;

    private FileRepositoryFactory() {
    }

    public static FileRepositoryFactory getInstance() {
        if (instance == null) {
            instance = new FileRepositoryFactory();
        }
        return instance;
    }

    public ICatalogoRepository createCatalogoRepository() {
        return new FileCatalogoRepository(DATA_CATALOGO);
    }

    public ICredenzialiRepository createCredenzialiRepository() {
        return new FileCredenzialiRepository(DATA_UTENTI);
    }

    public IBachecaRepository createBachecaRepository() {
        return new FileBachecaRepository(DATA_PROPOSTE);
    }

    public ISpazioPersonaleRepository createSpazioPersonaleRepository() {
        return new FileSpazioPersonaleRepository(DATA_NOTIFICHE);
    }
}
