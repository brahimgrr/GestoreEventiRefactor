package it.unibs.ingsoft.domain.model.utente;

import java.util.Locale;
import java.util.Objects;

public final class UserAccount {
    private final String username;
    private final String normalizedUsername;
    private final UserRole role;
    private final PasswordHash passwordHash;

    private UserAccount(String username, UserRole role, PasswordHash passwordHash) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }

        this.username = username.trim();
        this.normalizedUsername = normalize(username);
        this.role = Objects.requireNonNull(role);
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public static UserAccount create(String username, UserRole role, PasswordHash passwordHash) {
        return new UserAccount(username, role, passwordHash);
    }

    public static String normalize(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    public String username() {
        return username;
    }

    public String normalizedUsername() {
        return normalizedUsername;
    }

    public UserRole role() {
        return role;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }
}
