package it.unibs.ingsoft.presentation.view.interfaces.fruitore.notifica;

import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.notifica.Notifica;

import java.util.List;
import java.util.Optional;

public interface ISpazioPersonaleView {
    Optional<Notifica> selezionaNotificaDaEliminare(Fruitore fruitore, List<Notifica> notifiche);

    boolean confermaEliminazioneNotifica(Notifica notifica);

    void mostraNotificaEliminata(Notifica notifica);
}
