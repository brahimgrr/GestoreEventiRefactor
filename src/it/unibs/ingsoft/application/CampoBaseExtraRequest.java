package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.TipoDato;

/**
 * Dati necessari per configurare un campo base extra.
 */
public record CampoBaseExtraRequest(String nome, TipoDato tipoDato) {
    public CampoBaseExtraRequest {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome del campo non puo essere vuoto.");
        if (tipoDato == null)
            throw new IllegalArgumentException("Il tipo dato del campo non puo essere null.");

        nome = nome.trim();
    }
}
