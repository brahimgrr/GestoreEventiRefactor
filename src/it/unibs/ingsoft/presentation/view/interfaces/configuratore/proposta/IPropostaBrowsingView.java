package it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta;

import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;

import java.util.List;
import java.util.Map;

public interface IPropostaBrowsingView {
    void mostraBacheca(Map<String, List<Proposta>> bacheca);

    void mostraArchivioProposte(Map<StatoProposta, List<Proposta>> archivio);
}
