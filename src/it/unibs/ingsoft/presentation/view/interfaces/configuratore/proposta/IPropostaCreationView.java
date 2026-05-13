package it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPropostaCreationView {
    Optional<Categoria> selezionaCategoriaPerProposta(List<Categoria> categorie);

    Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator);

    Optional<Map<String, String>> correggiValoriProposta(
            Proposta proposta,
            PropostaValidationResult result,
            ProposalFieldValidator validator);

    void mostraPropostaSalvata(Proposta proposta);
}
