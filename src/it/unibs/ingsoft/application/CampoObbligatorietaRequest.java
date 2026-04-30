package it.unibs.ingsoft.application;

/**
 * Dati necessari per cambiare l'obbligatorieta di un campo.
 */
public record CampoObbligatorietaRequest(String nomeCampo, boolean obbligatorio) {
    public CampoObbligatorietaRequest {
        if (nomeCampo == null || nomeCampo.isBlank())
            throw new IllegalArgumentException("Il nome del campo non puo essere vuoto.");

        nomeCampo = nomeCampo.trim();
    }
}
