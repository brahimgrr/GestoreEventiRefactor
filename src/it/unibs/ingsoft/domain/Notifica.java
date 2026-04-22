package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notifica inviata a un fruitore in seguito a un cambio di stato di una proposta.
 *
 * <p>Invariante: {@code id} non è mai {@code null}.</p>
 */
public record Notifica(String id, String messaggio, LocalDateTime dataCreazione) {
    @JsonCreator
    public Notifica(@JsonProperty("id") String id,
                    @JsonProperty("messaggio") String messaggio,
                    @JsonProperty("dataCreazione") LocalDateTime dataCreazione) {
        this.id = id;
        this.messaggio = messaggio;
        this.dataCreazione = dataCreazione;
    }

    public Notifica(String messaggio) {
        this(UUID.randomUUID().toString(), messaggio, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notifica notifica = (Notifica) o;
        return id.equals(notifica.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
