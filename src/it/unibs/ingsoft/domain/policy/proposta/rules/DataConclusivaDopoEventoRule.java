package it.unibs.ingsoft.domain.policy.proposta.rules;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationContext;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;

import java.util.List;

public final class DataConclusivaDopoEventoRule implements PropostaValidationRule {
    @Override
    public void valida(PropostaValidationContext context, List<ValidationError> errors) {
        if (!context.deveValidareRelazioneTraCampi(
                AppConstants.CAMPO_DATA,
                AppConstants.CAMPO_DATA_CONCLUSIVA)) {
            return;
        }

        if (context.eventDate() != null
                && context.closingDate() != null
                && context.closingDate().isBefore(context.eventDate())) {
            errors.add(new ValidationError(
                    AppConstants.CAMPO_DATA_CONCLUSIVA,
                    new PropostaValidationFailure.ClosingDateBeforeEvent()));
        }
    }
}
