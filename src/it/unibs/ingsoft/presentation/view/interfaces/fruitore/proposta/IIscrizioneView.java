package it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta;

import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.shared.error.Failure;

import java.util.List;
import java.util.Optional;

public interface IIscrizioneView {
    boolean confermaIscrizione(Proposta proposta);

    Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte);

    boolean confermaDisiscrizione(Proposta proposta);

    void mostraIscrizioneEffettuata(Proposta proposta);

    void mostraDisiscrizioneEffettuata(Proposta proposta);

    void mostraErrore(Failure failure);
}
