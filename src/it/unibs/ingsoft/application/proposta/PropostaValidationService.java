package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Proposta;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Valida proposte e valori dei campi senza occuparsi di creazione o persistenza.
 */
public final class PropostaValidationService {
    public static boolean isTermineIscrizioneValido(LocalDate termine) {
        return Proposta.isTermineIscrizioneValido(termine);
    }

    public static boolean isDataEventoValida(LocalDate dataEvento, LocalDate termine) {
        return Proposta.isDataEventoValida(dataEvento, termine);
    }

    public static boolean isDataConclusivaValida(LocalDate conclusiva, LocalDate data) {
        return Proposta.isDataConclusivaValida(conclusiva, data);
    }

    public List<String> validaProposta(Proposta proposta) {
        return proposta.valida();
    }

    public List<String> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        return proposta.validaCampo(valoriCorrenti, nomeCampo, valore);
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
        proposta.aggiornaValoriCampi(valori);
        List<String> errori = validaProposta(proposta);
        return new PropostaValidationResult(
                errori.isEmpty(),
                errori,
                getCampiConErrore(proposta, errori)
        );
    }
}
