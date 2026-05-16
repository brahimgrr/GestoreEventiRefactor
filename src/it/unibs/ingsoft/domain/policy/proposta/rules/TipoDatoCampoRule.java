package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TipoDatoCampoRule implements PropostaValidationRule {
    private final TipoDatoValidator tipoDatoValidator;

    public TipoDatoCampoRule(TipoDatoValidator tipoDatoValidator) {
        this.tipoDatoValidator = Objects.requireNonNull(tipoDatoValidator);
    }

    @Override
    public List<ValidationError> valida(PropostaValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        for (Campo campo : context.campiDaValidare()) {
            validaCampo(campo, context, errors);
        }
        return errors;
    }

    private void validaCampo(Campo campo, PropostaValidationContext context, List<ValidationError> errors) {
        String value = context.valore(campo.getNome());

        if (value == null || value.isBlank()) {
            return;
        }

        tipoDatoValidator.validate(value, campo.getTipoDato())
                .map(error -> new ValidationError(campo.getNome(), error.failure()))
                .ifPresent(errors::add);
    }
}
