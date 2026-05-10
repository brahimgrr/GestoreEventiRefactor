package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.domain.EsitoTransizioneProposta;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.factory.NotificaFactory;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Coordina le transizioni di ciclo vita delle proposte gia' pubblicate.
 */
public final class PropostaLifecycleService {
    private final IBachecaRepository bachecaRepo;
    private final NotificationService notificationService;
    private final NotificaFactory notificaFactory;
    private final ReentrantLock lock = new ReentrantLock();

    public PropostaLifecycleService(IBachecaRepository bachecaRepo,
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
                EsitoTransizioneProposta esito = p.applicaTransizionePerScadenza(oggi);
                if (esito == EsitoTransizioneProposta.CONFERMATA) {
                    notificaAderenti(p, () -> notificaFactory.creaNotificaPropostaConfermata(p));
                    changed = true;
                } else if (esito == EsitoTransizioneProposta.ANNULLATA) {
                    notificaAderenti(p, () -> notificaFactory.creaNotificaPropostaAnnullata(p));
                    changed = true;
                } else if (esito == EsitoTransizioneProposta.CONCLUSA) {
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
            notificaAderenti(p, () -> notificaFactory.creaNotificaPropostaConfermata(p));
        } finally {
            lock.unlock();
        }
    }

    public void ritiraProposta(Proposta p) {
        lock.lock();
        try {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            p.ritira(oggi);
            notificaAderenti(p, () -> notificaFactory.creaNotificaPropostaRitirata(p));

            bachecaRepo.save();
        } finally {
            lock.unlock();
        }
    }

    private void notificaAderenti(Proposta p, Supplier<Notifica> notificaSupplier) {
        for (String aderente : p.getListaAderenti()) {
            notificationService.inviaNotifica(aderente, notificaSupplier.get());
        }
    }
}
