package it.unibs.ingsoft.domain.model.utente;

import it.unibs.ingsoft.domain.error.DomainException;

/**
 * Classe base astratta per tutti gli utenti dell'applicazione.
 */
public abstract class Persona {
    private final String username;

    /**
     * @throws IllegalArgumentException se username è null o blank
     * @pre username != null &amp;&amp; !username.isBlank()
     * @post getUsername().equals(username.trim ())
     * @post !getUsername().isBlank()
     */
    protected Persona(String username) {
        if (username == null || username.isBlank())
            throw new DomainException(new UserFailure.UsernameInvalid());
        this.username = username.trim();
    }

    public String getUsername() {
        return username;
    }

    /**
     * Due istanze di {@code Persona} sono uguali sse hanno lo stesso tipo concreto e lo stesso username.
     * Si usa {@code getClass()} (non {@code instanceof}) intenzionalmente: un {@code Configuratore}
     * e un {@code Fruitore} con lo stesso username sono entità distinte (ruoli diversi).
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        Persona persona = (Persona) o;
        return username.equals(persona.username);
    }

    @Override
    public final int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + username + "]";
    }
}
