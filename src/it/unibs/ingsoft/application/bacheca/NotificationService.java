package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.domain.ArchivioNotifiche;
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
        ArchivioNotifiche archivio = repo.load();
        SpazioPersonale sp = archivio.getOrCreateSpazioDi(username);
        sp.addNotifica(notifica);
        repo.save(archivio);
    }

    public List<Notifica> getNotifiche(String username) {
        if (username == null) return List.of();
        return repo.load()
                .findSpazioDi(username)
                .map(SpazioPersonale::getNotifiche)
                .orElseGet(List::of);
    }

    public void cancellaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        ArchivioNotifiche archivio = repo.load();
        SpazioPersonale sp = archivio.findSpazioDi(username).orElse(null);
        if (sp != null && sp.removeNotifica(notifica)) {
            repo.save(archivio);
        }
    }
}
