package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.DomainException;

/**
 * Dati necessari per cambiare l'obbligatorieta di un campo.
 */
public record CampoObbligatorietaRequest(String nomeCampo, boolean obbligatorio) {
    public CampoObbligatorietaRequest {
        if (nomeCampo == null || nomeCampo.isBlank())
            throw new DomainException(DomainErrorCode.CAMPO_NOME_NON_VALIDO);

        nomeCampo = nomeCampo.trim();
    }
}
