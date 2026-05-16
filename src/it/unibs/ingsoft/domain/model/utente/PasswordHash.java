package it.unibs.ingsoft.domain.model.utente;

import java.util.Objects;

public record PasswordHash(String hash) {
    public PasswordHash {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("hash must not be blank");
        }

        hash = hash.trim();
        Objects.requireNonNull(hash);
    }
}
