package it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta;

import it.unibs.ingsoft.domain.Proposta;

import java.util.List;
import java.util.Optional;

public interface IPropostaLifecycleView {
    Optional<Proposta> selezionaPropostaDaRitirare(List<Proposta> proposte);

    boolean confermaRitiro(Proposta proposta);
}
