package it.unibs.ingsoft.application.proposta.dto;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.validation.ValidationError;

import java.util.List;

/**
 * Risultato della validazione applicativa di una proposta.
 */
public record PropostaValidationResult(boolean valida, List<ValidationError> errori, List<Campo> campiConErrore) {
    public PropostaValidationResult {
        errori = List.copyOf(errori);
        campiConErrore = List.copyOf(campiConErrore);
    }
}
