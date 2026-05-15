package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidator;

import java.util.List;
import java.util.Objects;

public final class TipoDatoCampoRule implements PropostaValidationRule {
    private final TipoDatoValidator tipoDatoValidator;

    public TipoDatoCampoRule(TipoDatoValidator tipoDatoValidator) {
        this.tipoDatoValidator = Objects.requireNonNull(tipoDatoValidator);
    }

    @Override
    public void valida(PropostaValidationContext context, List<ValidationError> errors) {
        for (Campo campo : context.campiDaValidare()) {
            validaCampo(campo, context, errors);
        }
    }

    private void validaCampo(Campo campo, PropostaValidationContext context, List<ValidationError> errors) {
        String value = context.valoreDi(campo.getNome());

        if (value == null || value.isBlank()) {
            return;
        }

        tipoDatoValidator.validate(value, campo.getTipoDato())
                .map(error -> new ValidationError(campo.getNome(), error.failure()))
                .ifPresent(errors::add);
    }
}
