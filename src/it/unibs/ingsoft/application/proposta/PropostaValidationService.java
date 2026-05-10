package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.validation.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Valida proposte e valori dei campi senza occuparsi di creazione o persistenza.
 */
public final class PropostaValidationService {
    public List<ValidationError> validaProposta(Proposta proposta) {
        return proposta.valida();
    }

    public List<ValidationError> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        return proposta.validaCampo(valoriCorrenti, nomeCampo, valore);
    }

    public List<Campo> getCampiConErrore(Proposta proposta, List<ValidationError> errori) {
        return proposta.getCampi().stream()
                .filter(campo -> errori.stream().anyMatch(e -> campo.getNome().equals(e.fieldName())))
                .collect(Collectors.toList());
    }

    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        proposta.aggiornaValoriCampi(valori);
        List<ValidationError> errori = validaProposta(proposta);
        return new PropostaValidationResult(
                errori.isEmpty(),
                errori,
                getCampiConErrore(proposta, errori)
        );
    }
}
