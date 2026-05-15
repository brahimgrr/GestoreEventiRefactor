package it.unibs.ingsoft.application.notifica;

import it.unibs.ingsoft.domain.repository.NotificationRepository;
import it.unibs.ingsoft.domain.model.notifica.Notifica;

import java.util.List;
import java.util.Objects;

public final class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public void inviaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        repo.add(username, notifica);
    }

    public List<Notifica> getNotifiche(String username) {
        if (username == null) return List.of();

        return repo.findByUsername(username);
    }

    public void cancellaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        repo.delete(username, notifica.id());
    }
}
