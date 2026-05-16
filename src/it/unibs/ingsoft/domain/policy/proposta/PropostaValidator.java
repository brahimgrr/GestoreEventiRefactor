package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidator;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PropostaValidator {
    private final List<PropostaValidationRule> rules;
    private final Clock clock;

    public PropostaValidator(List<PropostaValidationRule> rules) {
        this(rules, AppConstants.clock);
    }

    public PropostaValidator(List<PropostaValidationRule> rules, Clock clock) {
        this.rules = List.copyOf(Objects.requireNonNull(rules));
        this.clock = Objects.requireNonNull(clock);
    }

    public static PropostaValidator standard() {
        return standard(AppConstants.clock);
    }

    public static PropostaValidator standard(Clock clock) {
        return new PropostaValidator(
                PropostaValidationRuleFactory.getRules(TipoDatoValidator.INSTANCE),
                clock);
    }

    public PropostaValidationOutcome valida(Proposta proposta) {
        PropostaValidationContext context = PropostaValidationContext.complete(proposta, clock);
        List<ValidationError> errors = run(context);
        return new PropostaValidationOutcome(
                errors,
                context.data(AppConstants.CAMPO_TERMINE_ISCRIZIONE),
                context.data(AppConstants.CAMPO_DATA));
    }

    public List<ValidationError> validaCampo(Campo campo, Map<String, String> valori) {
        PropostaValidationContext context =
                PropostaValidationContext.campoModificato(campo, valori, clock);
        return run(context);
    }

    private List<ValidationError> run(PropostaValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        for (PropostaValidationRule rule : rules) {
            errors.addAll(rule.valida(context));
        }
        return errors;
    }
}
