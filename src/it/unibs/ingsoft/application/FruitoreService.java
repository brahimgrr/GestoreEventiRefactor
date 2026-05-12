package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.bacheca.IscrizioneService;
import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.application.proposta.Proposta_Service;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.Proposta;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade dei casi d'uso disponibili al fruitore.
 */
public final class FruitoreService {
    private final Proposta_Service propostaService;
    private final IscrizioneService iscrizioneService;
    private final NotificationService notificationService;

    public FruitoreService(Proposta_Service propostaService,
                           IscrizioneService iscrizioneService,
                           NotificationService notificationService) {
        this.propostaService = Objects.requireNonNull(propostaService);
        this.iscrizioneService = Objects.requireNonNull(iscrizioneService);
        this.notificationService = Objects.requireNonNull(notificationService);
    }

    public Map<String, List<Proposta>> getBachecaPerCategoria() {
        return propostaService.getBachecaPerCategoria();
    }

    public void iscrivi(Proposta proposta, Fruitore fruitore) {
        iscrizioneService.iscrivi(proposta, fruitore);
    }

    public List<Proposta> getProposteAperteIscritteDa(Fruitore fruitore) {
        return propostaService.getProposteAperteIscritteDa(fruitore.getUsername());
    }

    public void disiscrivi(Proposta proposta, Fruitore fruitore) {
        iscrizioneService.disiscrivi(proposta, fruitore);
    }

    public List<Notifica> getNotifiche(Fruitore fruitore) {
        return notificationService.getNotifiche(fruitore.getUsername());
    }

    public void cancellaNotifica(Fruitore fruitore, Notifica notifica) {
        notificationService.cancellaNotifica(fruitore.getUsername(), notifica);
    }
}
