package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.FruitoreService;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca.IBachecaView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta.IIscrizioneView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.notifica.ISpazioPersonaleView;

import java.util.Objects;

public final class FruitoreController {
    private final IFruitoreView mainView;
    private final IBachecaView bachecaView;
    private final IIscrizioneView iscrizioneView;
    private final ISpazioPersonaleView spazioPersonaleView;
    private final FruitoreService fruitoreService;

    public FruitoreController(
            IFruitoreView mainView,
            IBachecaView bachecaView,
            IIscrizioneView iscrizioneView,
            ISpazioPersonaleView spazioPersonaleView,
            FruitoreService fruitoreService) {
        this.mainView = Objects.requireNonNull(mainView);
        this.bachecaView = Objects.requireNonNull(bachecaView);
        this.iscrizioneView = Objects.requireNonNull(iscrizioneView);
        this.spazioPersonaleView = Objects.requireNonNull(spazioPersonaleView);
        this.fruitoreService = Objects.requireNonNull(fruitoreService);
    }

    public void run(Fruitore fruitore) {
        Objects.requireNonNull(fruitore);
        while (true) {
            switch (mainView.scegliAzionePrincipale(fruitore)) {
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
        bachecaView.selezionaPropostaDaBacheca(fruitoreService.getBachecaPerCategoria())
                .filter(iscrizioneView::confermaIscrizione)
                .ifPresent(proposta -> esegui(
                        () -> fruitoreService.iscrivi(proposta, fruitore),
                        () -> iscrizioneView.mostraIscrizioneEffettuata(proposta)));
    }

    private void disdiciIscrizione(Fruitore fruitore) {
        iscrizioneView.selezionaPropostaDaDisdire(fruitoreService.getProposteAperteIscritteDa(fruitore))
                .filter(iscrizioneView::confermaDisiscrizione)
                .ifPresent(proposta -> esegui(
                        () -> fruitoreService.disiscrivi(proposta, fruitore),
                        () -> iscrizioneView.mostraDisiscrizioneEffettuata(proposta)));
    }

    private void gestisciSpazioPersonale(Fruitore fruitore) {
        while (true) {
            var scelta = spazioPersonaleView.selezionaNotificaDaEliminare(
                    fruitore,
                    fruitoreService.getNotifiche(fruitore));
            if (scelta.isEmpty()) {
                return;
            }

            scelta.filter(spazioPersonaleView::confermaEliminazioneNotifica)
                    .ifPresent(notifica -> {
                        fruitoreService.cancellaNotifica(fruitore, notifica);
                        spazioPersonaleView.mostraNotificaEliminata(notifica);
                    });
        }
    }

    private void esegui(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            iscrizioneView.mostraErrore(e);
        }
    }
}
