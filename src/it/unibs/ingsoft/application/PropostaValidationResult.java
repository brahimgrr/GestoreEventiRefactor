package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.Campo;

import java.util.List;

/**
 * Risultato della validazione applicativa di una proposta.
 */
public record PropostaValidationResult(boolean valida, List<String> errori, List<Campo> campiConErrore) {
    public PropostaValidationResult {
        errori = List.copyOf(errori);
        campiConErrore = List.copyOf(campiConErrore);
    }
}
