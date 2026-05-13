package it.unibs.ingsoft.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mappa {@code username → SpazioPersonale} persistita su file.
 * Crea uno spazio vuoto al primo accesso se l'utente non esiste ancora.
 */
public final class ArchivioNotificheDTO {
    private final Map<String, SpazioPersonaleDTO> utenti;

    public ArchivioNotificheDTO() {
        this.utenti = new HashMap<>();
    }

    @JsonCreator
    public static ArchivioNotificheDTO fromJson(
            @JsonProperty("utenti") Map<String, SpazioPersonaleDTO> utenti
    ) {
        ArchivioNotificheDTO a = new ArchivioNotificheDTO();
        if (utenti != null) {
            a.utenti.putAll(utenti);
        }
        return a;
    }

    public Map<String, SpazioPersonaleDTO> getUtenti() {
        return Collections.unmodifiableMap(utenti);
    }

    public Optional<SpazioPersonaleDTO> findSpazioDi(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(utenti.get(username));
    }

    public SpazioPersonaleDTO getOrCreateSpazioDi(String username) {
        return utenti.computeIfAbsent(username, k -> new SpazioPersonaleDTO());
    }

    /**
     * Restituisce lo spazio personale dell'utente, creandolo se necessario.
     *
     * @pre username != null
     * @post result != null
     */
    public SpazioPersonaleDTO getSpazioDi(String username) {
        return getOrCreateSpazioDi(username);
    }
}
