package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.notifica.NotificaType;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificaDocument(
        String id,
        NotificaType type,
        Map<String, String> payload,
        String messaggio,
        LocalDateTime dataCreazione) {

    public NotificaDocument {
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }

    public static NotificaDocument fromDomain(Notifica notifica) {
        return new NotificaDocument(
                notifica.id(),
                notifica.type(),
                notifica.payload(),
                notifica.messaggio(),
                notifica.dataCreazione());
    }

    public Notifica toDomain() {
        return new Notifica(id, type, payload, messaggio, dataCreazione);
    }
}
