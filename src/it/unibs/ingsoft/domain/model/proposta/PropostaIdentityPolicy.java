package it.unibs.ingsoft.domain.model.proposta;

import it.unibs.ingsoft.domain.AppConstants;

import java.util.Map;
import java.util.Objects;

/**
 * Regola di chiave naturale usata per rilevare proposte duplicate.
 * Non rappresenta l'identita' stabile dell'entita' Proposta.
 */
public final class PropostaIdentityPolicy {
    public static final PropostaIdentityPolicy DEFAULT = new PropostaIdentityPolicy();

    public String chiaveDuplicato(Proposta proposta) {
        Objects.requireNonNull(proposta);
        return chiaveDuplicato(proposta.getValoriCampi());
    }

    public String chiaveDuplicato(Map<String, String> valori) {
        Objects.requireNonNull(valori);
        return (valori.getOrDefault(AppConstants.CAMPO_TITOLO, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_DATA, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_ORA, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_LUOGO, "").trim()).toLowerCase();
    }
}
