package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.domain.repository.PropostaRepository;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.proposta.EsitoTransizioneProposta;
import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.notifica.NotificaFactory;
import it.unibs.ingsoft.domain.error.DomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Coordina le transizioni di ciclo vita delle proposte gia' pubblicate.
 */
public final class PropostaLifecycleService {
    private final PropostaRepository propostaRepo;
    private final NotificationService notificationService;
    private final NotificaFactory notificaFactory;
    private final PropostaCommandLock commandLock;

    public PropostaLifecycleService(PropostaRepository propostaRepo,
                                    NotificationService notificationService,
                                    NotificaFactory notificaFactory) {
        this(propostaRepo, notificationService, notificaFactory, new PropostaCommandLock());
    }

    public PropostaLifecycleService(PropostaRepository propostaRepo,
                                    NotificationService notificationService,
                                    NotificaFactory notificaFactory,
                                    PropostaCommandLock commandLock) {
        this.propostaRepo = Objects.requireNonNull(propostaRepo);
        this.notificationService = Objects.requireNonNull(notificationService);
        this.notificaFactory = Objects.requireNonNull(notificaFactory);
        this.commandLock = Objects.requireNonNull(commandLock);
    }

    public void controllaScadenze() {
        commandLock.runLocked(() -> {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            boolean changed = false;

            for (Proposta p : propostaRepo.findAll()) {
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

                if (changed) {
                    propostaRepo.save(p);
                    changed = false;
                }
            }
        });
    }

    public void confermaProposta(Proposta p) {
        commandLock.runLocked(() -> {
            String id = findSameIdentityId(p);
            propostaRepo.updateById(id, propostaPersistita -> confermaPropostaCaricata(propostaPersistita));
        });
    }

    private boolean confermaPropostaCaricata(Proposta p) {
        if (!p.confermaSeAperta()) return false;
        notificaAderenti(p, () -> notificaFactory.creaNotificaPropostaConfermata(p));
        return true;
    }

    public void ritiraProposta(Proposta p) {
        commandLock.runLocked(() -> {
            String id = findSameIdentityId(p);
            propostaRepo.updateById(id, propostaPersistita -> {
                LocalDate oggi = LocalDate.now(AppConstants.clock);
                propostaPersistita.ritira(oggi);
                notificaAderenti(
                        propostaPersistita,
                        () -> notificaFactory.creaNotificaPropostaRitirata(propostaPersistita));
                return null;
            });
        });
    }

    public void iscrivi(Proposta p, String username) {
        commandLock.runLocked(() -> {
            String id = findSameIdentityId(p);
            propostaRepo.updateById(id, propostaPersistita -> {
                LocalDate oggi = LocalDate.now(AppConstants.clock);
                propostaPersistita.iscrivi(username, oggi);

                if (propostaPersistita.haNumeroPartecipantiCompleto()) {
                    confermaPropostaCaricata(propostaPersistita);
                }
                return null;
            });
        });
    }

    public void disiscrivi(Proposta p, String username) {
        commandLock.runLocked(() -> {
            String id = findSameIdentityId(p);
            propostaRepo.updateById(id, propostaPersistita -> {
                propostaPersistita.disiscrivi(username, LocalDate.now(AppConstants.clock));
                return null;
            });
        });
    }

    private void notificaAderenti(Proposta p, Supplier<Notifica> notificaSupplier) {
        for (String aderente : p.getListaAderenti()) {
            notificationService.inviaNotifica(aderente, notificaSupplier.get());
        }
    }

    private String findSameIdentityId(Proposta proposta) {
        if (proposta == null) {
            throw new DomainException(new ProposalFailure.NotFound());
        }

        if (propostaRepo.findById(proposta.getId()).isPresent()) {
            return proposta.getId();
        }

        PropostaIdentityPolicy identityPolicy = PropostaIdentityPolicy.DEFAULT;
        String chiave = identityPolicy.chiaveDuplicato(proposta);
        return propostaRepo.findAll().stream()
                .filter(p -> chiave.equals(identityPolicy.chiaveDuplicato(p)))
                .findFirst()
                .map(Proposta::getId)
                .orElseThrow(() -> new DomainException(new ProposalFailure.NotFound()));
    }
}
