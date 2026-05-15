package it.unibs.ingsoft.domain.repository;

import it.unibs.ingsoft.domain.model.utente.UserAccount;

import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    void save(UserAccount account);
}
