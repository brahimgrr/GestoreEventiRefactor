package it.unibs.ingsoft.presentation.view.cli.fruitore;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.shared.error.Failure;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.IFruitoreViewFacade;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca.IBachecaView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.notifica.ISpazioPersonaleView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta.IIscrizioneView;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FruitoreViewFacade implements IFruitoreViewFacade {
    private final IFruitoreView mainView;
    private final IBachecaView bachecaView;
    private final IIscrizioneView iscrizioneView;
    private final ISpazioPersonaleView spazioPersonaleView;

    public FruitoreViewFacade(
            IFruitoreView mainView,
            IBachecaView bachecaView,
            IIscrizioneView iscrizioneView,
            ISpazioPersonaleView spazioPersonaleView) {
        this.mainView = Objects.requireNonNull(mainView);
        this.bachecaView = Objects.requireNonNull(bachecaView);
        this.iscrizioneView = Objects.requireNonNull(iscrizioneView);
        this.spazioPersonaleView = Objects.requireNonNull(spazioPersonaleView);
    }

    @Override
    public MainAction scegliAzionePrincipale(Fruitore fruitore) {
        return mainView.scegliAzionePrincipale(fruitore);
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca) {
        return bachecaView.selezionaPropostaDaBacheca(bacheca);
    }

    @Override
    public boolean confermaIscrizione(Proposta proposta) {
        return iscrizioneView.confermaIscrizione(proposta);
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte) {
        return iscrizioneView.selezionaPropostaDaDisdire(proposte);
    }

    @Override
    public boolean confermaDisiscrizione(Proposta proposta) {
        return iscrizioneView.confermaDisiscrizione(proposta);
    }

    @Override
    public void mostraIscrizioneEffettuata(Proposta proposta) {
        iscrizioneView.mostraIscrizioneEffettuata(proposta);
    }

    @Override
    public void mostraDisiscrizioneEffettuata(Proposta proposta) {
        iscrizioneView.mostraDisiscrizioneEffettuata(proposta);
    }

    @Override
    public void mostraErrore(Failure failure) {
        iscrizioneView.mostraErrore(failure);
    }

    @Override
    public Optional<Notifica> selezionaNotificaDaEliminare(Fruitore fruitore, List<Notifica> notifiche) {
        return spazioPersonaleView.selezionaNotificaDaEliminare(fruitore, notifiche);
    }

    @Override
    public boolean confermaEliminazioneNotifica(Notifica notifica) {
        return spazioPersonaleView.confermaEliminazioneNotifica(notifica);
    }

    @Override
    public void mostraNotificaEliminata(Notifica notifica) {
        spazioPersonaleView.mostraNotificaEliminata(notifica);
    }
}
