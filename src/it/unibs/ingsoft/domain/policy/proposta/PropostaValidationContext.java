package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.proposta.Proposta;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PropostaValidationContext {
    private final Map<String, String> valori;
    private final List<Campo> campi;
    private final Campo campoModificato;
    private final LocalDate today;

    private PropostaValidationContext(List<Campo> campi,
                                      Map<String, String> valori,
                                      Campo campoModificato,
                                      Clock clock) {
        this.campi = List.copyOf(Objects.requireNonNull(campi));
        this.valori = Collections.unmodifiableMap(new LinkedHashMap<>(valori));
        this.campoModificato = campoModificato;
        this.today = LocalDate.now(Objects.requireNonNull(clock));
    }

    public static PropostaValidationContext complete(Proposta proposta, Clock clock) {
        Objects.requireNonNull(proposta);
        return new PropostaValidationContext(proposta.getCampi(), proposta.getValoriCampi(), null, clock);
    }

    public static PropostaValidationContext campoModificato(Campo campo,
                                                            Map<String, String> valori,
                                                            Clock clock) {
        Objects.requireNonNull(campo);
        Objects.requireNonNull(valori);
        return new PropostaValidationContext(List.of(campo), valori, campo, clock);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), AppConstants.DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Campo> campi() {
        return campi;
    }

    public List<Campo> campiDaValidare() {
        if (isComplete()) {
            return campi;
        }
        return List.of(campoModificato);
    }

    public boolean isComplete() {
        return campoModificato == null;
    }

    public boolean deveValidareRelazioneTraCampi(String... nomiCampo) {
        return isComplete() || Arrays.stream(nomiCampo).anyMatch(this::isCampoModificato);
    }

    public String valore(String nomeCampo) {
        return valori.get(nomeCampo);
    }

    public LocalDate today() {
        return today;
    }

    public LocalDate data(String nomeCampo) {
        return parseDate(valore(nomeCampo));
    }

    private boolean isCampoModificato(String nomeCampo) {
        return campoModificato != null && Objects.equals(campoModificato.getNome(), nomeCampo);
    }
}
