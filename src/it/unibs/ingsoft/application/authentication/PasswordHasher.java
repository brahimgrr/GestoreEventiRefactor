package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.domain.model.utente.PasswordHash;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public final class PasswordHasher {
    private static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 120_000;
    private static final int DEFAULT_SALT_BYTES = 16;
    private static final int DEFAULT_KEY_BITS = 256;

    private final String algorithm;
    private final int iterations;
    private final int saltBytes;
    private final int keyBits;
    private final SecureRandom random;

    private PasswordHasher(String algorithm, int iterations, int saltBytes, int keyBits, SecureRandom random) {
        this.algorithm = Objects.requireNonNull(algorithm);
        this.iterations = iterations;
        this.saltBytes = saltBytes;
        this.keyBits = keyBits;
        this.random = Objects.requireNonNull(random);
    }

    public static PasswordHasher pbkdf2() {
        return new PasswordHasher(
                DEFAULT_ALGORITHM,
                DEFAULT_ITERATIONS,
                DEFAULT_SALT_BYTES,
                DEFAULT_KEY_BITS,
                new SecureRandom());
    }

    public PasswordHash hash(String password) {
        if (password == null) {
            throw new NullPointerException("password");
        }

        byte[] salt = new byte[saltBytes];
        random.nextBytes(salt);

        return new PasswordHash(
                algorithm,
                iterations,
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(derive(password.toCharArray(), salt, iterations, keyBits)));
    }

    public boolean matches(String password, PasswordHash expected) {
        if (password == null || expected == null) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(expected.salt());
        byte[] actualHash = derive(
                password.toCharArray(),
                salt,
                expected.iterations(),
                Base64.getDecoder().decode(expected.hash()).length * Byte.SIZE);
        String encodedActual = Base64.getEncoder().encodeToString(actualHash);
        return encodedActual.equals(expected.hash());
    }

    private byte[] derive(char[] password, byte[] salt, int iterations, int keyBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing algorithm unavailable", e);
        }
    }
}
