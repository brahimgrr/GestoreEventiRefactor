package it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta;

import it.unibs.ingsoft.domain.Proposta;

import java.util.List;
import java.util.Optional;

public interface IPropostaPublicationView {
    Optional<Proposta> selezionaPropostaDaPubblicare(List<Proposta> proposte);

    boolean confermaPubblicazione(Proposta proposta);

    void mostraPropostaPubblicata(Proposta proposta);
}
