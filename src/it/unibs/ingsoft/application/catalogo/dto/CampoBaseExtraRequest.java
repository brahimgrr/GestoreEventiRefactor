package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;

/**
 * Dati necessari per configurare un campo base extra.
 */
public record CampoBaseExtraRequest(String nome, TipoDato tipoDato) {
    public CampoBaseExtraRequest {
        if (nome == null || nome.isBlank())
            throw new DomainException(new CatalogFailure.FieldNameInvalid());
        if (tipoDato == null)
            throw new DomainException(new CatalogFailure.FieldDataTypeInvalid());

        nome = nome.trim();
    }
}
