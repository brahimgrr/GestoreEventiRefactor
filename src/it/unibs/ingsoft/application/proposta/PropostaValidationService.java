package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationOutcome;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Valida proposte e valori dei campi senza occuparsi di creazione o persistenza.
 */
public final class PropostaValidationService {
    private final PropostaValidator validator;

    public PropostaValidationService() {
        this(PropostaValidator.standard());
    }

    public PropostaValidationService(PropostaValidator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    public List<ValidationError> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        return validator.validaCampo(proposta, valoriCorrenti, nomeCampo, valore);
    }

    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        proposta.aggiornaValoriCampi(valori);
        PropostaValidationOutcome outcome = validator.valida(proposta);
        proposta.applicaEsitoValidazione(outcome);
        List<ValidationError> errori = outcome.errori();
        return new PropostaValidationResult(
                errori.isEmpty(),
                errori,
                proposta.getCampi().stream()
                        .filter(campo -> errori.stream().anyMatch(e -> campo.getNome().equals(e.fieldName())))
                        .collect(Collectors.toList())
        );
    }
}
