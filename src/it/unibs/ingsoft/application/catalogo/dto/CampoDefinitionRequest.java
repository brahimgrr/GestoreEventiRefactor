package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.DomainException;

/**
 * Dati necessari per creare un campo comune o specifico.
 */
public record CampoDefinitionRequest(String nome, TipoDato tipoDato, boolean obbligatorio) {
    public CampoDefinitionRequest {
        if (nome == null || nome.isBlank())
            throw new DomainException(DomainErrorCode.CAMPO_NOME_NON_VALIDO);
        if (tipoDato == null)
            throw new DomainException(DomainErrorCode.CAMPO_TIPO_DATO_NON_VALIDO);

        nome = nome.trim();
    }
}
