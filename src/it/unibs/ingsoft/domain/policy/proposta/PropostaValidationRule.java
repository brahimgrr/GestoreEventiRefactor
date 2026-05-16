package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.error.ValidationError;

import java.util.List;

@FunctionalInterface
public interface PropostaValidationRule {
    List<ValidationError> valida(PropostaValidationContext context);
}
