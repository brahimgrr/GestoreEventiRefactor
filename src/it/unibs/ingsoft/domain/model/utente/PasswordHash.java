package it.unibs.ingsoft.domain.model.utente;

import java.util.Objects;

public record PasswordHash(String algorithm, int iterations, String salt, String hash) {
    public PasswordHash {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("algorithm must not be blank");
        }
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations must be positive");
        }
        if (salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("salt must not be blank");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("hash must not be blank");
        }

        algorithm = algorithm.trim();
        salt = salt.trim();
        hash = hash.trim();
        Objects.requireNonNull(algorithm);
        Objects.requireNonNull(salt);
        Objects.requireNonNull(hash);
    }
}
