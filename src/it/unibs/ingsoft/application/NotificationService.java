package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.SpazioPersonale;
import it.unibs.ingsoft.persistence.api.ISpazioPersonaleRepository;

import java.util.List;
import java.util.Objects;

/**
 * Gestisce le notifiche dei fruitori tramite il loro {@link SpazioPersonale}.
 */
public final class NotificationService {

    private final ISpazioPersonaleRepository repo;

    public NotificationService(ISpazioPersonaleRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /**
     * Invia una notifica al fruitore indicato. Non fa nulla se i parametri sono null.
     *
     * @pre username != null &amp;&amp; notifica != null
     */
    public void inviaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        SpazioPersonale sp = repo.get(username);
        sp.addNotifica(notifica);
        repo.save();
    }

    /**
     * @pre username != null
     * @post result != null
     */
    public List<Notifica> getNotifiche(String username) {
        return repo.get(username).getNotifiche();
    }

    /**
     * Rimuove la notifica dallo spazio personale dell'utente.
     *
     * @pre username != null &amp;&amp; notifica != null
     */
    public void cancellaNotifica(String username, Notifica notifica) {
        SpazioPersonale sp = repo.get(username);
        sp.removeNotifica(notifica);
        repo.save();
    }
}
