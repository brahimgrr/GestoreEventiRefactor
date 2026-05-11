package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.proposta.Bacheca;
import it.unibs.ingsoft.domain.proposta.EsitoTransizioneProposta;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Coordina le transizioni di ciclo vita delle proposte gia' pubblicate.
 */
public final class PropostaLifecycleService {
    private final IBachecaRepository bachecaRepo;
    private final NotificationService notificationService;
    private final NotificaFactory notificaFactory;
    private final PropostaCommandLock commandLock;

    public PropostaLifecycleService(IBachecaRepository bachecaRepo,
                                    NotificationService notificationService,
                                    NotificaFactory notificaFactory) {
        this(bachecaRepo, notificationService, notificaFactory, new PropostaCommandLock());
    }

    public PropostaLifecycleService(IBachecaRepository bachecaRepo,
                                    NotificationService notificationService,
                                    NotificaFactory notificaFactory,
                                    PropostaCommandLock commandLock) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.notificationService = Objects.requireNonNull(notificationService);
        this.notificaFactory = Objects.requireNonNull(notificaFactory);
        this.commandLock = Objects.requireNonNull(commandLock);
    }

    public void controllaScadenze() {
        commandLock.runLocked(() -> {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            boolean changed = false;

            Bacheca bacheca = bachecaRepo.load();
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
                bachecaRepo.save(bacheca);
            }
        });
    }

    public void confermaProposta(Proposta p) {
        commandLock.runLocked(() -> {
            Bacheca bacheca = bachecaRepo.load();
            Proposta propostaPersistita = bacheca.findSameIdentityAs(p);
            if (confermaPropostaCaricata(propostaPersistita)) {
                bachecaRepo.save(bacheca);
            }
        });
    }

    private boolean confermaPropostaCaricata(Proposta p) {
        if (!p.confermaSeAperta()) return false;
        notificaAderenti(p, () -> notificaFactory.creaNotificaPropostaConfermata(p));
        return true;
    }

    public void ritiraProposta(Proposta p) {
        commandLock.runLocked(() -> {
            Bacheca bacheca = bachecaRepo.load();
            Proposta propostaPersistita = bacheca.findSameIdentityAs(p);
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            propostaPersistita.ritira(oggi);
            notificaAderenti(propostaPersistita, () -> notificaFactory.creaNotificaPropostaRitirata(propostaPersistita));

            bachecaRepo.save(bacheca);
        });
    }

    public void iscrivi(Proposta p, String username) {
        commandLock.runLocked(() -> {
            Bacheca bacheca = bachecaRepo.load();
            Proposta propostaPersistita = bacheca.findSameIdentityAs(p);
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            propostaPersistita.iscrivi(username, oggi);

            if (propostaPersistita.haNumeroPartecipantiCompleto()) {
                confermaPropostaCaricata(propostaPersistita);
            }

            bachecaRepo.save(bacheca);
        });
    }

    public void disiscrivi(Proposta p, String username) {
        commandLock.runLocked(() -> {
            Bacheca bacheca = bachecaRepo.load();
            Proposta propostaPersistita = bacheca.findSameIdentityAs(p);
            propostaPersistita.disiscrivi(username, LocalDate.now(AppConstants.clock));
            bachecaRepo.save(bacheca);
        });
    }

    private void notificaAderenti(Proposta p, Supplier<Notifica> notificaSupplier) {
        for (String aderente : p.getListaAderenti()) {
            notificationService.inviaNotifica(aderente, notificaSupplier.get());
        }
    }
}
