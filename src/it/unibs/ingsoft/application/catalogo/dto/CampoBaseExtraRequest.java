package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;

/**
 * Dati necessari per configurare un campo base extra.
 */
public record CampoBaseExtraRequest(String nome, TipoDato tipoDato) {
    public CampoBaseExtraRequest {
        if (nome == null || nome.isBlank())
            throw new DomainException(DomainErrorCode.CAMPO_NOME_NON_VALIDO);
        if (tipoDato == null)
            throw new DomainException(DomainErrorCode.CAMPO_TIPO_DATO_NON_VALIDO);

        nome = nome.trim();
    }
}
