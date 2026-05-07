package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.factory.NotificaFactory;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class StateTransitionService {

    private final IBachecaRepository bachecaRepo;
    private final NotificationService notificationService;
    private final NotificaFactory notificaFactory;
    private final ReentrantLock lock = new ReentrantLock();

    public StateTransitionService(IBachecaRepository bachecaRepo, NotificationService notificationService) {
        this(bachecaRepo, notificationService, NotificaFactory.getInstance());
    }

    public StateTransitionService(IBachecaRepository bachecaRepo,
                                  NotificationService notificationService,
                                  NotificaFactory notificaFactory) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.notificationService = Objects.requireNonNull(notificationService);
        this.notificaFactory = Objects.requireNonNull(notificaFactory);
    }

    public void controllaScadenze() {
        lock.lock();
        try {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            boolean changed = false;

            Bacheca bacheca = bachecaRepo.get();
            for (Proposta p : bacheca.getProposte()) {
                if (p.deveChiudereIscrizioni(oggi)) {
                    if (p.haNumeroPartecipantiCompleto()) {
                        confermaProposta(p);
                    } else {
                        annullaProposta(p);
                    }
                    changed = true;
                } else if (p.deveConcludersi(oggi)) {
                    concludiProposta(p);
                    changed = true;
                }
            }

            if (changed) {
                bachecaRepo.save();
            }
        } finally {
            lock.unlock();
        }
    }

    public void confermaProposta(Proposta p) {
        lock.lock();
        try {
            if (!p.confermaSeAperta()) return;

            String quota = p.valoreCampoOrDefault(AppConstants.CAMPO_QUOTA, "").trim();
            String info = "La proposta \"" + p.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo")
                    + "\" e' stata CONFERMATA.\n"
                    + "Data: " + p.valoreCampoOrDefault(AppConstants.CAMPO_DATA, "") + "\n"
                    + "Ora: " + p.valoreCampoOrDefault(AppConstants.CAMPO_ORA, "") + "\n"
                    + "Luogo: " + p.valoreCampoOrDefault(AppConstants.CAMPO_LUOGO, "") + "\n"
                    + (quota.isBlank() ? "" : "Quota: " + quota + "\n");

            String messaggio = info.trim();
            for (String aderente : p.getListaAderenti()) {
                notificationService.inviaNotifica(aderente, notificaFactory.creaNotifica(messaggio));
            }
        } finally {
            lock.unlock();
        }
    }

    public void ritiraProposta(Proposta p) {
        lock.lock();
        try {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            p.ritira(oggi);

            String titolo = p.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo");
            String messaggio = "La proposta \"" + titolo
                    + "\" e' stata RITIRATA dal configuratore.";
            for (String aderente : p.getListaAderenti()) {
                notificationService.inviaNotifica(aderente, notificaFactory.creaNotifica(messaggio));
            }

            bachecaRepo.save();
        } finally {
            lock.unlock();
        }
    }

    private void annullaProposta(Proposta p) {
        if (!p.annullaSeAperta()) return;

        String messaggio = "La proposta \"" + p.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Senza titolo")
                + "\" e' stata ANNULLATA per mancato raggiungimento del numero di partecipanti.";

        for (String aderente : p.getListaAderenti()) {
            notificationService.inviaNotifica(aderente, notificaFactory.creaNotifica(messaggio));
        }
    }

    private void concludiProposta(Proposta p) {
        p.concludiSeConfermata();
    }
}
