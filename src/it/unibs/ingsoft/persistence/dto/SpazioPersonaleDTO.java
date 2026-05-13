package it.unibs.ingsoft.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.utente.Fruitore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contenitore delle notifiche personali di un {@link Fruitore}.
 */
public final class SpazioPersonaleDTO {
    private final List<Notifica> notifiche;

    public SpazioPersonaleDTO() {
        this.notifiche = new ArrayList<>();
    }

    @JsonCreator
    public static SpazioPersonaleDTO fromJson(
            @JsonProperty("notifiche") List<Notifica> notifiche) {
        SpazioPersonaleDTO s = new SpazioPersonaleDTO();
        if (notifiche != null) {
            s.notifiche.addAll(notifiche);
        }
        return s;
    }

    public List<Notifica> getNotifiche() {
        return Collections.unmodifiableList(notifiche);
    }

    /**
     * Aggiunge {@code n} se non è già presente (confronto per ID).
     *
     * @pre n != null
     */
    public void addNotifica(Notifica n) {
        if (!notifiche.contains(n)) {
            notifiche.add(n);
        }
    }

    /**
     * Rimuove {@code n}; non fa nulla se non trovata.
     */
    public boolean removeNotifica(Notifica n) {
        return notifiche.remove(n);
    }
}
