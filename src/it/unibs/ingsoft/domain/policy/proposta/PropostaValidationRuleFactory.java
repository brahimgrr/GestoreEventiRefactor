package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.policy.proposta.rules.*;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidator;

import java.util.List;
import java.util.Objects;

public final class PropostaValidationRuleFactory {
    private PropostaValidationRuleFactory() {
    }

    public static List<PropostaValidationRule> getRules(TipoDatoValidator tipoDatoValidator) {
        Objects.requireNonNull(tipoDatoValidator);
        return List.of(
                new CampiObbligatoriRule(),
                new TipoDatoCampoRule(tipoDatoValidator),
                new TermineIscrizioneFuturoRule(),
                new DataEventoDopoTermineIscrizioneRule(),
                new DataConclusivaDopoEventoRule()
        );
    }
}
