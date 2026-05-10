package it.unibs.ingsoft.presentation.view.cli;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.validation.ValidationError;
import it.unibs.ingsoft.domain.validation.ValidationErrorParameter;

import java.util.List;

public final class ValidationMessageMapper {
    private ValidationMessageMapper() {
    }

    public static String message(ValidationError error) {
        String field = error.fieldName();
        return switch (error.code()) {
            case CAMPO_OBBLIGATORIO_MANCANTE -> "Campo obbligatorio mancante: \"" + field + "\".";
            case NUMERO_PARTECIPANTI_NON_INTERO ->
                    "\"" + AppConstants.CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero valido.";
            case NUMERO_PARTECIPANTI_NON_POSITIVO ->
                    "\"" + AppConstants.CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero positivo.";
            case TERMINE_ISCRIZIONE_NON_FUTURO -> {
                String oggi = error.parameter(ValidationErrorParameter.OGGI);
                yield "\"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna"
                        + (oggi == null ? "." : " (" + oggi + ").");
            }
            case DATA_EVENTO_TROPPO_PRESTO ->
                    "\"" + AppConstants.CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                            + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\".";
            case DATA_CONCLUSIVA_PRECEDENTE ->
                    "\"" + AppConstants.CAMPO_DATA_CONCLUSIVA + "\" non puo' essere precedente a \""
                            + AppConstants.CAMPO_DATA + "\".";
            case TIPO_INTERO_NON_VALIDO -> "Valore non valido: inserire un numero intero.";
            case TIPO_DECIMALE_NON_VALIDO -> "Valore non valido: inserire un numero decimale.";
            case TIPO_DATA_NON_VALIDA -> "Valore non valido: inserire una data nel formato "
                    + AppConstants.DATE_FORMAT_LABEL + " (es. 25/12/2026).";
            case TIPO_ORA_NON_VALIDA -> "Valore non valido: inserire una data nel formato hh:mm (es. 16:30).";
            case TIPO_BOOLEANO_NON_VALIDO -> "Valore non valido: inserire s/si/sì oppure n/no.";
        };
    }

    public static List<String> messages(List<ValidationError> errors) {
        return errors.stream().map(ValidationMessageMapper::message).toList();
    }
}
