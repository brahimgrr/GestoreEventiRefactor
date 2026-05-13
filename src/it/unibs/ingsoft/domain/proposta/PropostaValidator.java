package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.validation.DefaultTypeValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Valida i valori di una proposta tenendo insieme le regole correlate.
 */
public final class PropostaValidator {
    public List<ValidationError> valida(Proposta proposta) {
        return validaCompleta(proposta).errori();
    }

    public PropostaValidationOutcome validaCompleta(Proposta proposta) {
        Objects.requireNonNull(proposta);

        ParsedFields fields = parse(proposta.getValoriCampi());
        List<ValidationError> errori = new ArrayList<>();

        controllaCampi(proposta, errori);
        controllaNumeroPartecipanti(proposta.getValoriCampi(), errori);
        controllaTermineIscrizione(fields, true, errori);
        controllaDataEvento(fields, errori);
        controllaDataConclusiva(fields, errori);

        return new PropostaValidationOutcome(errori, fields.termineIscrizione(), fields.dataEvento());
    }

    public List<ValidationError> validaCampo(Proposta proposta,
                                             Map<String, String> valoriCorrenti,
                                             String nomeCampo,
                                             String valore) {
        Objects.requireNonNull(proposta);
        Map<String, String> valori = new LinkedHashMap<>(proposta.getValoriCampi());
        if (valoriCorrenti != null) {
            valori.putAll(valoriCorrenti);
        }
        valori.put(nomeCampo, valore);

        ParsedFields fields = parse(valori);
        List<ValidationError> errori = new ArrayList<>();

        if (AppConstants.CAMPO_NUM_PARTECIPANTI.equals(nomeCampo)) {
            Map<String, String> valorePartecipanti = new LinkedHashMap<>();
            valorePartecipanti.put(nomeCampo, valore);
            controllaNumeroPartecipanti(valorePartecipanti, errori);
        } else {
            controllaTipoCampo(proposta, nomeCampo, valore, errori);
        }

        switch (nomeCampo) {
            case AppConstants.CAMPO_TERMINE_ISCRIZIONE -> {
                controllaTermineIscrizione(fields, false, errori);
                controllaDataEvento(fields, errori);
            }
            case AppConstants.CAMPO_DATA -> {
                controllaDataEvento(fields, errori);
                controllaDataConclusiva(fields, errori);
            }
            case AppConstants.CAMPO_DATA_CONCLUSIVA -> controllaDataConclusiva(fields, errori);
            default -> {
            }
        }

        return errori;
    }

    private void controllaCampi(Proposta proposta, List<ValidationError> errori) {
        for (Campo campo : proposta.getCampi()) {
            String valore = proposta.getValoriCampi().get(campo.getNome());
            if (campo.isObbligatorio()) {
                if (valore == null || valore.isBlank()) {
                    errori.add(new ValidationError(
                            campo.getNome(),
                            new ProposalValidationFailure.RequiredFieldMissing(campo.getNome())));
                }
            }
            controllaTipoCampo(campo, valore, errori);
        }
    }

    private void controllaTipoCampo(Proposta proposta,
                                    String nomeCampo,
                                    String valore,
                                    List<ValidationError> errori) {
        proposta.getCampi().stream()
                .filter(campo -> campo.getNome().equals(nomeCampo))
                .findFirst()
                .ifPresent(campo -> controllaTipoCampo(campo, valore, errori));
    }

    private void controllaTipoCampo(Campo campo, String valore, List<ValidationError> errori) {
        if (valore == null || valore.isBlank()) {
            return;
        }
        if (AppConstants.CAMPO_NUM_PARTECIPANTI.equals(campo.getNome())) {
            return;
        }

        DefaultTypeValidator.INSTANCE.validate(valore, campo.getTipoDato())
                .map(errore -> new ValidationError(campo.getNome(), errore.failure()))
                .ifPresent(errori::add);
    }

    private void controllaNumeroPartecipanti(Map<String, String> valori, List<ValidationError> errori) {
        String raw = valori.get(AppConstants.CAMPO_NUM_PARTECIPANTI);
        if (raw == null || raw.isBlank()) {
            return;
        }

        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                errori.add(new ValidationError(
                        AppConstants.CAMPO_NUM_PARTECIPANTI,
                        new ProposalValidationFailure.ParticipantsNotPositive()));
            }
        } catch (NumberFormatException e) {
            errori.add(new ValidationError(
                    AppConstants.CAMPO_NUM_PARTECIPANTI,
                    new ProposalValidationFailure.ParticipantsNotInteger()));
        }
    }

    private void controllaTermineIscrizione(ParsedFields fields,
                                            boolean includeOggiNelDettaglio,
                                            List<ValidationError> errori) {
        LocalDate termine = fields.termineIscrizione();
        if (termine == null || termine.isAfter(fields.oggi())) {
            return;
        }

        if (includeOggiNelDettaglio) {
            errori.add(new ValidationError(
                    AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                    new ProposalValidationFailure.SubscriptionDeadlineNotFuture(fields.oggi())));
        } else {
            errori.add(new ValidationError(
                    AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                    new ProposalValidationFailure.SubscriptionDeadlineNotFuture(null)));
        }
    }

    private void controllaDataEvento(ParsedFields fields, List<ValidationError> errori) {
        if (fields.termineIscrizione() != null
                && fields.dataEvento() != null
                && !fields.dataEvento().isAfter(fields.termineIscrizione().plusDays(1))) {
            errori.add(new ValidationError(
                    AppConstants.CAMPO_DATA,
                    new ProposalValidationFailure.EventDateTooEarly()));
        }
    }

    private void controllaDataConclusiva(ParsedFields fields, List<ValidationError> errori) {
        if (fields.dataEvento() != null
                && fields.dataConclusiva() != null
                && fields.dataConclusiva().isBefore(fields.dataEvento())) {
            errori.add(new ValidationError(
                    AppConstants.CAMPO_DATA_CONCLUSIVA,
                    new ProposalValidationFailure.ClosingDateBeforeEvent()));
        }
    }

    private ParsedFields parse(Map<String, String> valori) {
        return new ParsedFields(
                LocalDate.now(AppConstants.clock),
                parseData(valori.get(AppConstants.CAMPO_TERMINE_ISCRIZIONE)),
                parseData(valori.get(AppConstants.CAMPO_DATA)),
                parseData(valori.get(AppConstants.CAMPO_DATA_CONCLUSIVA))
        );
    }

    private LocalDate parseData(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), AppConstants.DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private record ParsedFields(
            LocalDate oggi,
            LocalDate termineIscrizione,
            LocalDate dataEvento,
            LocalDate dataConclusiva) {
    }
}
