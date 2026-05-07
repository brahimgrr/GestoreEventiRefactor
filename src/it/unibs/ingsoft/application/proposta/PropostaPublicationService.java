package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.domain.factory.NotificaFactory;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestisce salvataggio in memoria e pubblicazione delle proposte valide.
 */
public final class PropostaPublicationService {
    private final IBachecaRepository bachecaRepo;
    private final PropostaQueryService queryService;
    private final NotificationService notificationService;
    private final NotificaFactory notificaFactory;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Proposta> proposteValide = new ArrayList<>();

    public PropostaPublicationService(IBachecaRepository bachecaRepo) {
        this(bachecaRepo, new PropostaQueryService(bachecaRepo));
    }

    public PropostaPublicationService(IBachecaRepository bachecaRepo, PropostaQueryService queryService) {
        this(bachecaRepo, queryService, null, NotificaFactory.getInstance());
    }

    public PropostaPublicationService(IBachecaRepository bachecaRepo,
                                      PropostaQueryService queryService,
                                      NotificationService notificationService,
                                      NotificaFactory notificaFactory) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.queryService = Objects.requireNonNull(queryService);
        this.notificationService = notificationService;
        this.notificaFactory = Objects.requireNonNull(notificaFactory);
    }

    private Bacheca bacheca() {
        return bachecaRepo.get();
    }

    public void salvaProposta(Proposta proposta) {
        proposta.verificaSalvabile();
        rilevaDuplicatoAlSalvataggio(proposta);
        proposteValide.add(proposta);
    }

    public List<Proposta> getProposteValide() {
        return Collections.unmodifiableList(proposteValide);
    }

    public void rimuoviPropostaValida(Proposta proposta) {
        proposteValide.remove(proposta);
    }

    public void clearProposteValide() {
        proposteValide.clear();
    }

    public void pubblicaProposta(Proposta proposta) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        proposta.verificaPubblicabile(oggi);
        rilevaDuplicato(proposta);

        proposta.pubblica(oggi);
        bacheca().addProposta(proposta);
        bachecaRepo.save();
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
                    notificaAderenti(p, NotificaFormatter.propostaConfermata(p));
                    changed = true;
                } else if (esito == EsitoTransizioneProposta.ANNULLATA) {
                    notificaAderenti(p, NotificaFormatter.propostaAnnullata(p));
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
            notificaAderenti(p, NotificaFormatter.propostaConfermata(p));
        } finally {
            lock.unlock();
        }
    }

    public void ritiraProposta(Proposta p) {
        lock.lock();
        try {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            p.ritira(oggi);
            notificaAderenti(p, NotificaFormatter.propostaRitirata(p));

            bachecaRepo.save();
        } finally {
            lock.unlock();
        }
    }

    private void notificaAderenti(Proposta p, String messaggio) {
        for (String aderente : p.getListaAderenti()) {
            notificationService.inviaNotifica(aderente, notificaFactory.creaNotifica(messaggio));
        }
    }

    private void rilevaDuplicatoAlSalvataggio(Proposta proposta) {
        String chiave = proposta.getChiaveIdentita();

        boolean inBacheca = queryService.getTutteLeProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));
        boolean inValide = proposteValide.stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (inBacheca || inValide) {
            throw new IllegalStateException(
                    "Esiste gia' una proposta con lo stesso Titolo, Data, Ora e Luogo.");
        }
    }

    private void rilevaDuplicato(Proposta proposta) {
        String chiave = proposta.getChiaveIdentita();

        boolean duplicato = queryService.getTutteLeProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (duplicato) {
            throw new IllegalStateException("Esiste gia' una proposta con lo stesso Titolo, Data, Ora e Luogo.");
        }
    }
}
