package it.unibs.ingsoft.presentation.view.interfaces;

import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.validation.ValidationError;

import java.util.List;
import java.util.Map;

/**
 * Validatore di un singolo campo nel contesto della proposta in fase di compilazione.
 * Usato dalla view per il feedback inline (business rules, vincoli di data).
 *
 * @return lista di messaggi di errore; vuota se il valore è valido
 */
@FunctionalInterface
public interface ProposalFieldValidator {
    List<ValidationError> validate(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore);
}
