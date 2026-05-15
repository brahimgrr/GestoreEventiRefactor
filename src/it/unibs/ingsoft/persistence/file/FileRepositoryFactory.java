package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.repository.CatalogoRepository;
import it.unibs.ingsoft.domain.repository.NotificationRepository;
import it.unibs.ingsoft.domain.repository.PropostaRepository;
import it.unibs.ingsoft.domain.repository.UserRepository;

import java.nio.file.Path;
import java.util.Objects;

public final class FileRepositoryFactory {
    private static final Path DEFAULT_DATA_DIR = Path.of("data/v6");

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

    public CatalogoRepository createCatalogoRepository() {
        return new FileCatalogoRepository(dataDir.resolve("catalogo.json"));
    }

    public UserRepository createUserRepository() {
        return new FileUserRepository(dataDir.resolve("users.json"));
    }

    public PropostaRepository createPropostaRepository() {
        return new FilePropostaRepository(dataDir.resolve("proposte.json"));
    }

    public NotificationRepository createNotificationRepository() {
        return new FileNotificationRepository(dataDir.resolve("notifiche.json"));
    }
}
