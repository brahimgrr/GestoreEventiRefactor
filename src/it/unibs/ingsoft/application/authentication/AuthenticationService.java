package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.application.error.ApplicationException;
import it.unibs.ingsoft.domain.model.utente.*;
import it.unibs.ingsoft.domain.repository.UserRepository;
import it.unibs.ingsoft.shared.error.Failure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Gestisce autenticazione e registrazione di configuratori e fruitori.
 *
 * <p>Il primo accesso usa le credenziali predefinite ({@code config/config}).
 * Dopo il login con tali credenziali, il controller forza la registrazione
 * di credenziali personali prima di consentire qualsiasi operazione.</p>
 */
public final class AuthenticationService {
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final String PASSWORD_ALGORITHM = "SHA-256";

    private final UserRepository repo;
    private final UtenteFactory utenteFactory;

    /**
     * @pre repo   != null
     */
    public AuthenticationService(UserRepository repo) {
        this(repo, UtenteFactory.getInstance());
    }

    public AuthenticationService(UserRepository repo, UtenteFactory utenteFactory) {
        this.repo = Objects.requireNonNull(repo);
        this.utenteFactory = Objects.requireNonNull(utenteFactory);
    }

    private static void validaCredenziali(String username, String password) {
        if (username == null || username.isBlank())
            throw new ApplicationException(new AuthenticationFailure.UsernameInvalid());

        if (password == null || password.isBlank())
            throw new ApplicationException(new AuthenticationFailure.PasswordInvalid());

        if (username.length() < MIN_USERNAME_LENGTH)
            throw new ApplicationException(new AuthenticationFailure.UsernameTooShort(MIN_USERNAME_LENGTH));
        if (password.length() < MIN_PASSWORD_LENGTH)
            throw new ApplicationException(new AuthenticationFailure.PasswordTooShort(MIN_PASSWORD_LENGTH));
    }

    public boolean isConfiguratorePredefinito(Configuratore configuratore) {
        return configuratore != null &&
                USERNAME_PREDEFINITO.equals(configuratore.getUsername());
    }

    public Optional<Failure> validaNuovoUsername(String username) {
        if (username == null || username.isBlank() || username.trim().length() < MIN_USERNAME_LENGTH)
            return Optional.of(new AuthenticationFailure.UsernameTooShort(MIN_USERNAME_LENGTH));

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(username.trim()))
            return Optional.of(new AuthenticationFailure.UsernameReserved(username));

        if (esisteUsername(username))
            return Optional.of(new AuthenticationFailure.UsernameAlreadyInUse(username));

        return Optional.empty();
    }

    public Optional<Failure> validaNuovaPassword(String password) {
        if (password == null || password.isBlank() || password.trim().length() < MIN_PASSWORD_LENGTH)
            return Optional.of(new AuthenticationFailure.PasswordTooShort(MIN_PASSWORD_LENGTH));
        return Optional.empty();
    }

    /**
     * Tenta il login con le credenziali fornite.
     *
     * @return il configuratore autenticato, o empty se le credenziali non sono valide
     */
    public Optional<Configuratore> login(String username, String password) {
        if (username == null || password == null)
            return Optional.empty();

        // Le credenziali predefinite condivise rimangono disponibili per i flussi di primo accesso.
        if (USERNAME_PREDEFINITO.equals(username) &&
                PASSWORD_PREDEFINITA.equals(password) &&
                !repo.existsByUsername(USERNAME_PREDEFINITO))
            return Optional.of(utenteFactory.creaConfiguratore(USERNAME_PREDEFINITO));

        Optional<UserAccount> account = repo.findByUsername(username);
        if (account.isPresent() &&
                account.get().role() == UserRole.CONFIGURATORE &&
                passwordMatches(password, account.get().passwordHash())) {
            return Optional.of(utenteFactory.creaConfiguratore(username));
        }

        return Optional.empty();
    }

    /**
     * Tenta il login di un fruitore.
     *
     * @return il fruitore autenticato, o empty se le credenziali non sono valide
     */
    public Optional<Fruitore> loginFruitore(String username, String password) {
        if (username == null || password == null)
            return Optional.empty();

        Optional<UserAccount> account = repo.findByUsername(username);
        if (account.isPresent() &&
                account.get().role() == UserRole.FRUITORE &&
                passwordMatches(password, account.get().passwordHash())) {
            return Optional.of(utenteFactory.creaFruitore(username));
        }

        return Optional.empty();
    }

    /**
     * Registra un nuovo configuratore con le credenziali personali.
     *
     * @throws IllegalArgumentException se le credenziali sono riservate, duplicate o troppo corte
     */
    public Configuratore registraNuovoConfiguratore(String username, String password) {
        validaNuovoAccount(username, password);

        String normalized = username.trim();
        repo.save(UserAccount.create(normalized, UserRole.CONFIGURATORE, hashPassword(password)));
        return utenteFactory.creaConfiguratore(normalized);
    }

    /**
     * Registra un nuovo fruitore con le credenziali fornite.
     *
     * @throws IllegalArgumentException se le credenziali sono riservate, duplicate o troppo corte
     * @post esisteUsername(username)
     */
    public Fruitore registraNuovoFruitore(String username, String password) {
        validaNuovoAccount(username, password);

        String normalized = username.trim();
        repo.save(UserAccount.create(normalized, UserRole.FRUITORE, hashPassword(password)));
        return utenteFactory.creaFruitore(normalized);
    }

    private static PasswordHash hashPassword(String password) {
        return new PasswordHash(digest(password));
    }

    private static boolean passwordMatches(String password, PasswordHash expected) {
        return expected != null
                && digest(password).equals(expected.hash());
    }

    private static String digest(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(PASSWORD_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Password hashing algorithm unavailable", e);
        }
    }

    private void validaNuovoAccount(String username, String password) {
        validaCredenziali(username, password);

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(username))
            throw new ApplicationException(new AuthenticationFailure.UsernameReserved(username));

        if (esisteUsername(username))
            throw new ApplicationException(new AuthenticationFailure.UsernameAlreadyInUse(username));
    }

    /**
     * Restituisce true se un account con questo username e' gia' registrato (in qualsiasi ruolo).
     */
    public boolean esisteUsername(String username) {
        return username != null && repo.existsByUsername(username);
    }
}
