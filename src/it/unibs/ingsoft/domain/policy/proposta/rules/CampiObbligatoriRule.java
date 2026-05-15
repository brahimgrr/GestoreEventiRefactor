package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.util.List;

public final class CampiObbligatoriRule implements PropostaValidationRule {
    @Override
    public void valida(PropostaValidationContext context, List<ValidationError> errors) {
        if (!context.isComplete()) {
            return;
        }

        for (Campo campo : context.campi()) {
            String value = context.valoreDi(campo.getNome());
            if (campo.isObbligatorio() && (value == null || value.isBlank())) {
                errors.add(new ValidationError(
                        campo.getNome(),
                        new PropostaValidationFailure.RequiredFieldMissing(campo.getNome())));
            }
        }
    }
}
