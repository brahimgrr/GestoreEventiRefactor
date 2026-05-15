package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.time.LocalDate;
import java.util.List;

public final class TermineIscrizioneFuturoRule implements PropostaValidationRule {
    @Override
    public void valida(PropostaValidationContext context, List<ValidationError> errors) {
        if (!context.deveValidareRelazioneTraCampi(AppConstants.CAMPO_TERMINE_ISCRIZIONE)) {
            return;
        }

        LocalDate deadline = context.subscriptionDeadline();
        if (deadline == null || deadline.isAfter(context.today())) {
            return;
        }

        errors.add(new ValidationError(
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                new PropostaValidationFailure.SubscriptionDeadlineNotFuture(
                        context.isComplete() ? context.today() : null)));
    }
}
