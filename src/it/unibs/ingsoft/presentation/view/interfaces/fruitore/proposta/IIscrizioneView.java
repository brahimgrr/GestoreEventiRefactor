package it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta;

import it.unibs.ingsoft.domain.Proposta;

import java.util.List;
import java.util.Optional;

public interface IIscrizioneView {
    boolean confermaIscrizione(Proposta proposta);

    Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte);

    boolean confermaDisiscrizione(Proposta proposta);

    void mostraIscrizioneEffettuata(Proposta proposta);

    void mostraDisiscrizioneEffettuata(Proposta proposta);

    void mostraErrore(Exception e);
}
