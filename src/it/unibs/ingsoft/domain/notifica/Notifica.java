package it.unibs.ingsoft.domain.notifica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Notifica inviata a un fruitore in seguito a un cambio di stato di una proposta.
 *
 * <p>Invariante: {@code id} non è mai {@code null}.</p>
 */
public record Notifica(
        String id,
        NotificaType type,
        Map<String, String> payload,
        String messaggio,
        LocalDateTime dataCreazione) {
    @JsonCreator
    public Notifica(@JsonProperty("id") String id,
                    @JsonProperty("type") NotificaType type,
                    @JsonProperty("payload") Map<String, String> payload,
                    @JsonProperty("messaggio") String messaggio,
                    @JsonProperty("dataCreazione") LocalDateTime dataCreazione) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.type = type == null ? NotificaType.LEGACY_MESSAGGIO : type;
        this.payload = payload == null ? Map.of() : Map.copyOf(payload);
        this.messaggio = messaggio;
        this.dataCreazione = dataCreazione == null ? LocalDateTime.now() : dataCreazione;
    }

    public Notifica(String messaggio) {
        this(UUID.randomUUID().toString(), NotificaType.LEGACY_MESSAGGIO, Map.of(), messaggio, LocalDateTime.now());
    }

    public static Notifica notificaStrutturata(NotificaType type, Map<String, String> payload) {
        return new Notifica(UUID.randomUUID().toString(), type, payload, null, LocalDateTime.now());
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
