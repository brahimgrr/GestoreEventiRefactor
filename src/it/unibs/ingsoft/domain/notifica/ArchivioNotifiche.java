package it.unibs.ingsoft.domain.notifica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.utente.SpazioPersonale;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mappa {@code username → SpazioPersonale} persistita su file.
 * Crea uno spazio vuoto al primo accesso se l'utente non esiste ancora.
 */
public final class ArchivioNotifiche {
    private final Map<String, SpazioPersonale> utenti;

    public ArchivioNotifiche() {
        this.utenti = new HashMap<>();
    }

    @JsonCreator
    public static ArchivioNotifiche fromJson(
            @JsonProperty("utenti") Map<String, SpazioPersonale> utenti
    ) {
        ArchivioNotifiche a = new ArchivioNotifiche();
        if (utenti != null) {
            a.utenti.putAll(utenti);
        }
        return a;
    }

    public Map<String, SpazioPersonale> getUtenti() {
        return Collections.unmodifiableMap(utenti);
    }

    public Optional<SpazioPersonale> findSpazioDi(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(utenti.get(username));
    }

    public SpazioPersonale getOrCreateSpazioDi(String username) {
        return utenti.computeIfAbsent(username, k -> new SpazioPersonale());
    }

    /**
     * Restituisce lo spazio personale dell'utente, creandolo se necessario.
     *
     * @pre username != null
     * @post result != null
     */
    public SpazioPersonale getSpazioDi(String username) {
        return getOrCreateSpazioDi(username);
    }
}
