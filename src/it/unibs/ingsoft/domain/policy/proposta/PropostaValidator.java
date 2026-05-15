package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
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
        return new PropostaValidationOutcome(errors, context.subscriptionDeadline(), context.eventDate());
    }

    public List<ValidationError> validaCampo(Proposta proposta,
                                             Map<String, String> valoriCorrenti,
                                             String nomeCampo,
                                             String valore) {
        PropostaValidationContext context =
                PropostaValidationContext.campoModificato(proposta, valoriCorrenti, nomeCampo, valore, clock);
        return run(context);
    }

    private List<ValidationError> run(PropostaValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        for (PropostaValidationRule rule : rules) {
            rule.valida(context, errors);
        }
        return errors;
    }
}
