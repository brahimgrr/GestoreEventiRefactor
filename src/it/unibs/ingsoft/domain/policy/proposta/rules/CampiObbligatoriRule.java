package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.util.ArrayList;
import java.util.List;

public final class CampiObbligatoriRule implements PropostaValidationRule {
    @Override
    public List<ValidationError> valida(PropostaValidationContext context) {
        if (!context.isComplete()) {
            return List.of();
        }

        List<ValidationError> errors = new ArrayList<>();
        for (Campo campo : context.campi()) {
            String value = context.valore(campo.getNome());
            if (campo.isObbligatorio() && (value == null || value.isBlank())) {
                errors.add(new ValidationError(
                        campo.getNome(),
                        new PropostaValidationFailure.RequiredFieldMissing(campo.getNome())));
            }
        }
        return errors;
    }
}
