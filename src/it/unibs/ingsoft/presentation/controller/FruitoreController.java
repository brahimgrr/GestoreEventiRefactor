package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.IscrizioneService;
import it.unibs.ingsoft.application.NotificationService;
import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.IFruitoreView;

import java.util.Objects;

public final class FruitoreController {
    private final IFruitoreView view;
    private final PropostaService propostaService;
    private final IscrizioneService iscrizioneService;
    private final NotificationService notificationService;

    public FruitoreController(
            IFruitoreView view,
            PropostaService propostaService,
            IscrizioneService iscrizioneService,
            NotificationService notificationService) {
        this.view = Objects.requireNonNull(view);
        this.propostaService = Objects.requireNonNull(propostaService);
        this.iscrizioneService = Objects.requireNonNull(iscrizioneService);
        this.notificationService = Objects.requireNonNull(notificationService);
    }

    public void run(Fruitore fruitore) {
        Objects.requireNonNull(fruitore);
        while (true) {
            switch (view.scegliAzionePrincipale(fruitore)) {
                case BACHECA -> iscrizioneDaBacheca(fruitore);
                case DISDICI_ISCRIZIONE -> disdiciIscrizione(fruitore);
                case SPAZIO_PERSONALE -> gestisciSpazioPersonale(fruitore);
                case LOGOUT -> {
                    return;
                }
            }
        }
    }

    private void iscrizioneDaBacheca(Fruitore fruitore) {
        view.selezionaPropostaDaBacheca(propostaService.getBachecaPerCategoria())
                .filter(view::confermaIscrizione)
                .ifPresent(proposta -> esegui(
                        () -> iscrizioneService.iscrivi(proposta, fruitore),
                        () -> view.mostraIscrizioneEffettuata(proposta)));
    }

    private void disdiciIscrizione(Fruitore fruitore) {
        view.selezionaPropostaDaDisdire(propostaService.getProposteAperteIscritteDa(fruitore.getUsername()))
                .filter(view::confermaDisiscrizione)
                .ifPresent(proposta -> esegui(
                        () -> iscrizioneService.disiscrivi(proposta, fruitore),
                        () -> view.mostraDisiscrizioneEffettuata(proposta)));
    }

    private void gestisciSpazioPersonale(Fruitore fruitore) {
        while (true) {
            var scelta = view.selezionaNotificaDaEliminare(
                    fruitore,
                    notificationService.getNotifiche(fruitore.getUsername()));
            if (scelta.isEmpty()) {
                return;
            }

            scelta.filter(view::confermaEliminazioneNotifica)
                    .ifPresent(notifica -> {
                        notificationService.cancellaNotifica(fruitore.getUsername(), notifica);
                        view.mostraNotificaEliminata(notifica);
                    });
        }
    }

    private void esegui(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.mostraErrore(e);
        }
    }
}
