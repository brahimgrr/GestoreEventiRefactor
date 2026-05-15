package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.utente.PasswordHash;
import it.unibs.ingsoft.domain.model.utente.UserAccount;
import it.unibs.ingsoft.domain.model.utente.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record UserStoreDocument(List<UserRecord> users) {
    public UserStoreDocument {
        users = users == null ? List.of() : List.copyOf(users);
    }

    public static UserStoreDocument empty() {
        return new UserStoreDocument(List.of());
    }

    public Optional<UserAccount> findByUsername(String username) {
        String normalized = UserAccount.normalize(username);
        if (normalized == null) {
            return Optional.empty();
        }

        return users.stream()
                .filter(user -> normalized.equals(user.normalizedUsername()))
                .findFirst()
                .map(UserRecord::toDomain);
    }

    public UserStoreDocument save(UserAccount account) {
        List<UserRecord> next = new ArrayList<>(users);
        next.removeIf(user -> account.normalizedUsername().equals(user.normalizedUsername()));
        next.add(UserRecord.fromDomain(account));
        return new UserStoreDocument(next);
    }

    public record UserRecord(
            String username,
            String normalizedUsername,
            UserRole role,
            String algorithm,
            int iterations,
            String salt,
            String hash) {

        static UserRecord fromDomain(UserAccount account) {
            PasswordHash passwordHash = account.passwordHash();
            return new UserRecord(
                    account.username(),
                    account.normalizedUsername(),
                    account.role(),
                    passwordHash.algorithm(),
                    passwordHash.iterations(),
                    passwordHash.salt(),
                    passwordHash.hash());
        }

        UserAccount toDomain() {
            return UserAccount.create(
                    username,
                    role,
                    new PasswordHash(algorithm, iterations, salt, hash));
        }
    }
}
