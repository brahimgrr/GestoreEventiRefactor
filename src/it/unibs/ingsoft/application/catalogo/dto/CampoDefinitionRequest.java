package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;

/**
 * Dati necessari per creare un campo comune o specifico.
 */
public record CampoDefinitionRequest(String nome, TipoDato tipoDato, boolean obbligatorio) {
    public CampoDefinitionRequest {
        if (nome == null || nome.isBlank())
            throw new DomainException(new CatalogFailure.FieldNameInvalid());
        if (tipoDato == null)
            throw new DomainException(new CatalogFailure.FieldDataTypeInvalid());

        nome = nome.trim();
    }
}
