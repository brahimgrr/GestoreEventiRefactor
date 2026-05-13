package it.unibs.ingsoft.application.authentication;

import it.unibs.ingsoft.application.error.ApplicationException;
import it.unibs.ingsoft.domain.shared.error.Failure;
import it.unibs.ingsoft.domain.utente.Configuratore;
import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.utente.UtenteFactory;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;

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

    private final ICredenzialiRepository repo;
    private final UtenteFactory utenteFactory;

    /**
     * @pre repo   != null
     */
    public AuthenticationService(ICredenzialiRepository repo) {
        this(repo, UtenteFactory.getInstance());
    }

    public AuthenticationService(ICredenzialiRepository repo, UtenteFactory utenteFactory) {
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

    private CredenzialiDTO credenziali() {
        return repo.load();
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
                PASSWORD_PREDEFINITA.equals(password))
            return Optional.of(utenteFactory.creaConfiguratore(USERNAME_PREDEFINITO));

        String key = username.trim().toLowerCase();
        String stored = credenziali().getConfiguratori().get(key);
        if (stored != null && stored.equals(password))
            return Optional.of(utenteFactory.creaConfiguratore(username));

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

        String key = username.trim().toLowerCase();
        String stored = credenziali().getFruitori().get(key);
        if (stored != null && stored.equals(password))
            return Optional.of(utenteFactory.creaFruitore(username));

        return Optional.empty();
    }

    /**
     * Registra un nuovo configuratore con le credenziali personali.
     *
     * @throws IllegalArgumentException se le credenziali sono riservate, duplicate o troppo corte
     */
    public Configuratore registraNuovoConfiguratore(String username, String password) {
        CredenzialiDTO credenziali = repo.load();
        validaNuovoAccount(username, password, credenziali);

        String normalized = username.trim();
        credenziali.addConfiguratore(normalized, password);
        repo.save(credenziali);
        return utenteFactory.creaConfiguratore(normalized);
    }

    /**
     * Registra un nuovo fruitore con le credenziali fornite.
     *
     * @throws IllegalArgumentException se le credenziali sono riservate, duplicate o troppo corte
     * @post esisteUsername(username)
     */
    public Fruitore registraNuovoFruitore(String username, String password) {
        CredenzialiDTO credenziali = repo.load();
        validaNuovoAccount(username, password, credenziali);

        String normalized = username.trim();
        credenziali.addFruitore(normalized, password);
        repo.save(credenziali);
        return utenteFactory.creaFruitore(normalized);
    }

    private void validaNuovoAccount(String username, String password) {
        validaNuovoAccount(username, password, repo.load());
    }

    private void validaNuovoAccount(String username, String password, CredenzialiDTO credenziali) {
        validaCredenziali(username, password);

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(username))
            throw new ApplicationException(new AuthenticationFailure.UsernameReserved(username));

        if (esisteUsername(username, credenziali))
            throw new ApplicationException(new AuthenticationFailure.UsernameAlreadyInUse(username));
    }

    /**
     * Restituisce true se un account con questo username è già registrato (in qualsiasi ruolo).
     */
    public boolean esisteUsername(String username) {
        return esisteUsername(username, repo.load());
    }

    private boolean esisteUsername(String username, CredenzialiDTO credenziali) {
        if (username == null) return false;
        String u = username.trim().toLowerCase();
        return credenziali.getConfiguratori().containsKey(u) ||
                credenziali.getFruitori().containsKey(u);
    }
}
