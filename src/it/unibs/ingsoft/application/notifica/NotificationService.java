package it.unibs.ingsoft.application.notifica;

import it.unibs.ingsoft.persistence.dto.ArchivioNotificheDTO;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.persistence.dto.SpazioPersonaleDTO;
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
        ArchivioNotificheDTO archivio = repo.load();
        SpazioPersonaleDTO sp = archivio.getOrCreateSpazioDi(username);
        sp.addNotifica(notifica);

        repo.save(archivio);
    }

    public List<Notifica> getNotifiche(String username) {
        if (username == null) return List.of();

        return repo.load()
                .findSpazioDi(username)
                .map(SpazioPersonaleDTO::getNotifiche)
                .orElseGet(List::of);
    }

    public void cancellaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        ArchivioNotificheDTO archivio = repo.load();
        SpazioPersonaleDTO sp = archivio.findSpazioDi(username).orElse(null);
        if (sp != null && sp.removeNotifica(notifica)) {
            repo.save(archivio);
        }
    }
}
