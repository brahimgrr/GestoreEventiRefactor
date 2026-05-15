package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.utente.UserAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record NotificationStoreDocument(Map<String, List<NotificaDocument>> notifichePerUtente) {
    public NotificationStoreDocument {
        if (notifichePerUtente == null) {
            notifichePerUtente = Map.of();
        } else {
            Map<String, List<NotificaDocument>> copy = new LinkedHashMap<>();
            notifichePerUtente.forEach((username, notifiche) ->
                    copy.put(username, notifiche == null ? List.of() : List.copyOf(notifiche)));
            notifichePerUtente = Map.copyOf(copy);
        }
    }

    public static NotificationStoreDocument empty() {
        return new NotificationStoreDocument(Map.of());
    }

    public List<Notifica> findByUsername(String username) {
        String key = UserAccount.normalize(username);
        if (key == null) {
            return List.of();
        }
        return notifichePerUtente.getOrDefault(key, List.of()).stream()
                .map(NotificaDocument::toDomain)
                .toList();
    }

    public NotificationStoreDocument add(String username, Notifica notifica) {
        String key = UserAccount.normalize(username);
        if (key == null || notifica == null) {
            return this;
        }

        Map<String, List<NotificaDocument>> next = mutableCopy();
        List<NotificaDocument> notifiche = next.computeIfAbsent(key, ignored -> new ArrayList<>());
        NotificaDocument document = NotificaDocument.fromDomain(notifica);
        if (notifiche.stream().noneMatch(existing -> document.id().equals(existing.id()))) {
            notifiche.add(document);
        }
        return new NotificationStoreDocument(next);
    }

    public DeleteResult delete(String username, String notificationId) {
        String key = UserAccount.normalize(username);
        if (key == null || notificationId == null) {
            return new DeleteResult(this, false);
        }

        Map<String, List<NotificaDocument>> next = mutableCopy();
        List<NotificaDocument> notifiche = next.get(key);
        if (notifiche == null) {
            return new DeleteResult(this, false);
        }

        boolean removed = notifiche.removeIf(notifica -> notificationId.equals(notifica.id()));
        return new DeleteResult(new NotificationStoreDocument(next), removed);
    }

    private Map<String, List<NotificaDocument>> mutableCopy() {
        Map<String, List<NotificaDocument>> next = new LinkedHashMap<>();
        notifichePerUtente.forEach((username, notifiche) -> next.put(username, new ArrayList<>(notifiche)));
        return next;
    }

    public record DeleteResult(NotificationStoreDocument document, boolean removed) {
    }
}
