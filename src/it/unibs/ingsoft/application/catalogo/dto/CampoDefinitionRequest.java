package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.TipoDato;

/**
 * Dati necessari per creare un campo comune o specifico.
 */
public record CampoDefinitionRequest(String nome, TipoDato tipoDato, boolean obbligatorio) {
    public CampoDefinitionRequest {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome del campo non puo essere vuoto.");
        if (tipoDato == null)
            throw new IllegalArgumentException("Il tipo dato del campo non puo essere null.");

        nome = nome.trim();
    }
}
