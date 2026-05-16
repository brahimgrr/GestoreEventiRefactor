package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.util.List;

public final class DataEventoDopoTermineIscrizioneRule implements PropostaValidationRule {
    @Override
    public List<ValidationError> valida(PropostaValidationContext context) {
        if (!context.deveValidareRelazioneTraCampi(
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                AppConstants.CAMPO_DATA)) {
            return List.of();
        }

        var subscriptionDeadline = context.data(AppConstants.CAMPO_TERMINE_ISCRIZIONE);
        var eventDate = context.data(AppConstants.CAMPO_DATA);
        if (subscriptionDeadline != null
                && eventDate != null
                && !eventDate.isAfter(subscriptionDeadline.plusDays(1))) {
            return List.of(new ValidationError(
                    AppConstants.CAMPO_DATA,
                    new PropostaValidationFailure.EventDateTooEarly()));
        }
        return List.of();
    }
}
