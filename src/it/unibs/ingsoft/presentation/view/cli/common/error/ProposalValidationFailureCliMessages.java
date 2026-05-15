package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;

public final class ProposalValidationFailureCliMessages {
    private ProposalValidationFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry.register(PropostaValidationFailure.RequiredFieldMissing.class,
                        (failure, messages) -> "Campo obbligatorio mancante: \"" + failure.fieldName() + "\".")
                .register(PropostaValidationFailure.SubscriptionDeadlineNotFuture.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna" + (failure.today() == null ? "." : " (" + failure.today() + ")."))
                .register(PropostaValidationFailure.EventDateTooEarly.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\".")
                .register(PropostaValidationFailure.ClosingDateBeforeEvent.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_DATA_CONCLUSIVA + "\" non puo' essere precedente a \"" + AppConstants.CAMPO_DATA + "\".")
                .register(PropostaValidationFailure.ParticipantTooShort.class,
                        (currentHeighy, minHeight) -> "Sei troppo basso, min height: " + minHeight);
    }
}

