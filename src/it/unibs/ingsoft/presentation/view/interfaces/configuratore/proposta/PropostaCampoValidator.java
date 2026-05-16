package it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;

import java.util.List;
import java.util.Map;

/**
 * Validatore di un singolo campo nel contesto della proposta in fase di compilazione.
 * Usato dalla view per il feedback inline (business rules, vincoli di data).
 *
 * @return lista di messaggi di errore; vuota se il valore è valido
 */
@FunctionalInterface
public interface PropostaCampoValidator {
    List<ValidationError> valida(Campo campo, Map<String, String> valori);
}
