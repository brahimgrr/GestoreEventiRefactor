package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.FruitoreService;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.shared.error.FailureException;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.IFruitoreViewFacade;

import java.util.Objects;

public final class FruitoreController {
    private final IFruitoreViewFacade view;
    private final FruitoreService fruitoreService;

    public FruitoreController(
            IFruitoreViewFacade view,
            FruitoreService fruitoreService) {
        this.view = Objects.requireNonNull(view);
        this.fruitoreService = Objects.requireNonNull(fruitoreService);
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
        view.selezionaPropostaDaBacheca(fruitoreService.getBachecaPerCategoria())
                .filter(view::confermaIscrizione)
                .ifPresent(proposta -> esegui(
                        () -> fruitoreService.iscrivi(proposta, fruitore),
                        () -> view.mostraIscrizioneEffettuata(proposta)));
    }

    private void disdiciIscrizione(Fruitore fruitore) {
        view.selezionaPropostaDaDisdire(fruitoreService.getProposteAperteIscritteDa(fruitore))
                .filter(view::confermaDisiscrizione)
                .ifPresent(proposta -> esegui(
                        () -> fruitoreService.disiscrivi(proposta, fruitore),
                        () -> view.mostraDisiscrizioneEffettuata(proposta)));
    }

    private void gestisciSpazioPersonale(Fruitore fruitore) {
        while (true) {
            var scelta = view.selezionaNotificaDaEliminare(
                    fruitore,
                    fruitoreService.getNotifiche(fruitore));
            if (scelta.isEmpty()) {
                return;
            }

            scelta.filter(view::confermaEliminazioneNotifica)
                    .ifPresent(notifica -> {
                        fruitoreService.cancellaNotifica(fruitore, notifica);
                        view.mostraNotificaEliminata(notifica);
                    });
        }
    }

    private void esegui(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (FailureException e) {
            view.mostraErrore(e.failure());
        }
    }
}
