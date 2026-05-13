package it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca;

import it.unibs.ingsoft.domain.Proposta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IBachecaView {
    Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca);
}
