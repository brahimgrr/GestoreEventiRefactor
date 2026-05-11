package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.proposta.Proposta;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade dei casi d'uso disponibili al fruitore.
 */
public final class FruitoreService {
    private final PropostaService propostaService;
    private final NotificationService notificationService;

    public FruitoreService(PropostaService propostaService,
                           NotificationService notificationService) {
        this.propostaService = Objects.requireNonNull(propostaService);
        this.notificationService = Objects.requireNonNull(notificationService);
    }

    public Map<String, List<Proposta>> getBachecaPerCategoria() {
        return propostaService.getBachecaPerCategoria();
    }

    public void iscrivi(Proposta proposta, Fruitore fruitore) {
        propostaService.iscrivi(proposta, fruitore.getUsername());
    }

    public List<Proposta> getProposteAperteIscritteDa(Fruitore fruitore) {
        return propostaService.getProposteAperteIscritteDa(fruitore.getUsername());
    }

    public void disiscrivi(Proposta proposta, Fruitore fruitore) {
        propostaService.disiscrivi(proposta, fruitore.getUsername());
    }

    public List<Notifica> getNotifiche(Fruitore fruitore) {
        return notificationService.getNotifiche(fruitore.getUsername());
    }

    public void cancellaNotifica(Fruitore fruitore, Notifica notifica) {
        notificationService.cancellaNotifica(fruitore.getUsername(), notifica);
    }
}
