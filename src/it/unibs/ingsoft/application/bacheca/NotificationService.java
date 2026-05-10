package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.SpazioPersonale;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;

import java.util.List;
import java.util.Objects;

public final class NotificationService {

    private final ISpazioPersonaleRepository repo;

    public NotificationService(ISpazioPersonaleRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public void inviaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        SpazioPersonale sp = repo.get(username);
        sp.addNotifica(notifica);
        repo.save();
    }

    public List<Notifica> getNotifiche(String username) {
        return repo.get(username).getNotifiche();
    }

    public void cancellaNotifica(String username, Notifica notifica) {
        SpazioPersonale sp = repo.get(username);
        sp.removeNotifica(notifica);
        repo.save();
    }
}
