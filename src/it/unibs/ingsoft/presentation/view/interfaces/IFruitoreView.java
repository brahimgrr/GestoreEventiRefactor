package it.unibs.ingsoft.presentation.view.interfaces;

import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.Proposta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IFruitoreView {
    enum MainAction {
        BACHECA,
        DISDICI_ISCRIZIONE,
        SPAZIO_PERSONALE,
        LOGOUT
    }

    MainAction scegliAzionePrincipale(Fruitore fruitore);

    Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca);

    boolean confermaIscrizione(Proposta proposta);

    Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte);

    boolean confermaDisiscrizione(Proposta proposta);

    Optional<Notifica> selezionaNotificaDaEliminare(Fruitore fruitore, List<Notifica> notifiche);

    boolean confermaEliminazioneNotifica(Notifica notifica);

    void mostraIscrizioneEffettuata(Proposta proposta);

    void mostraDisiscrizioneEffettuata(Proposta proposta);

    void mostraNotificaEliminata(Notifica notifica);

    void mostraErrore(Exception e);
}
