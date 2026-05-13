package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.proposta.ProposalValidationFailure;
import it.unibs.ingsoft.domain.shared.AppConstants;

public final class ProposalValidationFailureCliMessages {
    private ProposalValidationFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry.register(ProposalValidationFailure.RequiredFieldMissing.class,
                        (failure, messages) -> "Campo obbligatorio mancante: \"" + failure.fieldName() + "\".")
                .register(ProposalValidationFailure.ParticipantsNotInteger.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero valido.")
                .register(ProposalValidationFailure.ParticipantsNotPositive.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero positivo.")
                .register(ProposalValidationFailure.SubscriptionDeadlineNotFuture.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna" + (failure.today() == null ? "." : " (" + failure.today() + ")."))
                .register(ProposalValidationFailure.EventDateTooEarly.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\".")
                .register(ProposalValidationFailure.ClosingDateBeforeEvent.class,
                        (failure, messages) -> "\"" + AppConstants.CAMPO_DATA_CONCLUSIVA + "\" non puo' essere precedente a \"" + AppConstants.CAMPO_DATA + "\".")
                .register(ProposalValidationFailure.ParticipantTooShort.class,
                        (currentHeighy, minHeight) -> "Sei troppo basso, min height: " + minHeight);
    }
}


