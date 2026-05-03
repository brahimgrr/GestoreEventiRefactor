package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Valida proposte e valori dei campi senza occuparsi di creazione o persistenza.
 */
public final class PropostaValidationService {
    private static final String CAMPO_TERMINE_ISCRIZIONE = AppConstants.CAMPO_TERMINE_ISCRIZIONE;
    private static final String CAMPO_DATA = AppConstants.CAMPO_DATA;
    private static final String CAMPO_DATA_CONCLUSIVA = AppConstants.CAMPO_DATA_CONCLUSIVA;
    private static final String CAMPO_NUM_PARTECIPANTI = AppConstants.CAMPO_NUM_PARTECIPANTI;

    public static boolean isTermineIscrizioneValido(LocalDate termine) {
        return termine != null && termine.isAfter(LocalDate.now(AppConstants.clock));
    }

    public static boolean isDataEventoValida(LocalDate dataEvento, LocalDate termine) {
        return dataEvento != null && termine != null && dataEvento.isAfter(termine.plusDays(1));
    }

    public static boolean isDataConclusivaValida(LocalDate conclusiva, LocalDate data) {
        return conclusiva != null && data != null && !conclusiva.isBefore(data);
    }

    public List<String> validaProposta(Proposta proposta) {
        proposta.revertToBozzaSilent();

        List<String> errori = new ArrayList<>();
        Map<String, String> valori = proposta.getValoriCampi();

        controllaCampiObbligatori(proposta.getCampi(), valori, errori);
        controllaNumeroPartecipanti(valori, errori);

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null && !isTermineIscrizioneValido(termineIscr)) {
            errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna (" + oggi + ").");
        }

        if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr)) {
            errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                    + CAMPO_TERMINE_ISCRIZIONE + "\".");
        }

        if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento)) {
            errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non puÃ² essere precedente a \"" + CAMPO_DATA + "\".");
        }

        if (errori.isEmpty()) {
            proposta.setTermineIscrizione(termineIscr);
            proposta.setDataEvento(dataEvento);
            proposta.setStato(StatoProposta.VALIDA);
        }

        return errori;
    }

    public List<String> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        Map<String, String> valori = new LinkedHashMap<>(proposta.getValoriCampi());
        valori.putAll(valoriCorrenti);
        valori.put(nomeCampo, valore);

        List<String> errori = new ArrayList<>();
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        switch (nomeCampo) {
            case CAMPO_TERMINE_ISCRIZIONE:
                if (termineIscr != null && !isTermineIscrizioneValido(termineIscr)) {
                    errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna.");
                }
                if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr)) {
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" +
                            CAMPO_TERMINE_ISCRIZIONE + "\".");
                }
                break;

            case CAMPO_DATA:
                if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr)) {
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" +
                            CAMPO_TERMINE_ISCRIZIONE + "\".");
                }
                if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento)) {
                    errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non puÃ² essere precedente a \"" + CAMPO_DATA + "\".");
                }
                break;

            case CAMPO_DATA_CONCLUSIVA:
                if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento)) {
                    errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non puÃ² essere precedente a \"" + CAMPO_DATA + "\".");
                }
                break;

            default:
                break;
        }

        return errori;
    }

    public List<Campo> getCampiConErrore(Proposta proposta, List<String> errori) {
        return proposta.getCampi().stream()
                .filter(campo -> {
                    String quoted = "\"" + campo.getNome() + "\"";
                    return errori.stream().anyMatch(e -> e.contains(quoted));
                })
                .collect(Collectors.toList());
    }

    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        proposta.putAllValoriCampi(valori);
        List<String> errori = validaProposta(proposta);
        return new PropostaValidationResult(
                errori.isEmpty(),
                errori,
                getCampiConErrore(proposta, errori)
        );
    }

    private void controllaCampiObbligatori(List<Campo> campi, Map<String, String> valori, List<String> errori) {
        for (Campo campo : campi) {
            if (campo.isObbligatorio()) {
                String valore = valori.get(campo.getNome());
                if (valore == null || valore.isBlank()) {
                    errori.add("Campo obbligatorio mancante: \"" + campo.getNome() + "\".");
                }
            }
        }
    }

    private void controllaNumeroPartecipanti(Map<String, String> valori, List<String> errori) {
        String numStr = valori.get(CAMPO_NUM_PARTECIPANTI);
        if (numStr == null || numStr.isBlank()) {
            return;
        }

        try {
            int n = Integer.parseInt(numStr.trim());
            if (n <= 0) {
                errori.add("\"" + CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero positivo.");
            }
        } catch (NumberFormatException e) {
            errori.add("\"" + CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero valido.");
        }
    }

    private LocalDate parseData(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
