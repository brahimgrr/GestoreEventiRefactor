package it.unibs.ingsoft.domain.repository;

import it.unibs.ingsoft.domain.model.notifica.Notifica;

import java.util.List;

public interface NotificationRepository {
    List<Notifica> findByUsername(String username);

    void add(String username, Notifica notifica);

    boolean delete(String username, String notificationId);
}
