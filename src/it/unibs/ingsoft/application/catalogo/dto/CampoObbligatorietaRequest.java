package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.shared.error.DomainException;

/**
 * Dati necessari per cambiare l'obbligatorieta di un campo.
 */
public record CampoObbligatorietaRequest(String nomeCampo, boolean obbligatorio) {
    public CampoObbligatorietaRequest {
        if (nomeCampo == null || nomeCampo.isBlank())
            throw new DomainException(new CatalogFailure.FieldNameInvalid());

        nomeCampo = nomeCampo.trim();
    }
}
