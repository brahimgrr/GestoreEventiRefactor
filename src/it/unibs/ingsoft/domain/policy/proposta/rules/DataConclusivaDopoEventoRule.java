package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.util.List;

public final class DataConclusivaDopoEventoRule implements PropostaValidationRule {
    @Override
    public List<ValidationError> valida(PropostaValidationContext context) {
        if (!context.deveValidareRelazioneTraCampi(
                AppConstants.CAMPO_DATA,
                AppConstants.CAMPO_DATA_CONCLUSIVA)) {
            return List.of();
        }

        var eventDate = context.data(AppConstants.CAMPO_DATA);
        var closingDate = context.data(AppConstants.CAMPO_DATA_CONCLUSIVA);
        if (eventDate != null
                && closingDate != null
                && closingDate.isBefore(eventDate)) {
            return List.of(new ValidationError(
                    AppConstants.CAMPO_DATA_CONCLUSIVA,
                    new PropostaValidationFailure.ClosingDateBeforeEvent()));
        }
        return List.of();
    }
}
