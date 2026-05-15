package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.repository.UserRepository;
import it.unibs.ingsoft.domain.model.utente.UserAccount;
import it.unibs.ingsoft.persistence.file.document.UserStoreDocument;

import java.nio.file.Path;
import java.util.Optional;

public final class FileUserRepository
        extends AbstractFileRepository<UserStoreDocument>
        implements UserRepository {

    public FileUserRepository(Path path) {
        super(path, UserStoreDocument.class, UserStoreDocument::empty);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return super.load().findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public void save(UserAccount account) {
        super.save(super.load().save(account));
    }
}
