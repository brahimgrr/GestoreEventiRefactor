package it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca;

import it.unibs.ingsoft.domain.proposta.Proposta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IBachecaView {
    Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca);
}
