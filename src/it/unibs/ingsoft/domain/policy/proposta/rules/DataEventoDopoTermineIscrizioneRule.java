package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.util.List;

public final class DataEventoDopoTermineIscrizioneRule implements PropostaValidationRule {
    @Override
    public void valida(PropostaValidationContext context, List<ValidationError> errors) {
        if (!context.deveValidareRelazioneTraCampi(
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                AppConstants.CAMPO_DATA)) {
            return;
        }

        if (context.subscriptionDeadline() != null
                && context.eventDate() != null
                && !context.eventDate().isAfter(context.subscriptionDeadline().plusDays(1))) {
            errors.add(new ValidationError(
                    AppConstants.CAMPO_DATA,
                    new PropostaValidationFailure.EventDateTooEarly()));
        }
    }
}
