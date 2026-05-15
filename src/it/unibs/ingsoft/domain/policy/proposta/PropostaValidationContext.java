package it.unibs.ingsoft.domain.policy.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.proposta.Proposta;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

public final class PropostaValidationContext {
    private final Map<String, String> valori;
    private final List<Campo> campi;
    private final boolean complete;
    private final String nomeCampoModificato;
    private final LocalDate today;
    private final LocalDate subscriptionDeadline;
    private final LocalDate eventDate;
    private final LocalDate closingDate;

    private PropostaValidationContext(Proposta proposta,
                                      Map<String, String> valori,
                                      boolean complete,
                                      String nomeCampoModificato,
                                      Clock clock) {
        Objects.requireNonNull(proposta);
        this.valori = Collections.unmodifiableMap(new LinkedHashMap<>(valori));
        this.campi = List.copyOf(proposta.getCampi());
        this.complete = complete;
        this.nomeCampoModificato = nomeCampoModificato;
        this.today = LocalDate.now(Objects.requireNonNull(clock));
        this.subscriptionDeadline = parseDate(this.valori.get(AppConstants.CAMPO_TERMINE_ISCRIZIONE));
        this.eventDate = parseDate(this.valori.get(AppConstants.CAMPO_DATA));
        this.closingDate = parseDate(this.valori.get(AppConstants.CAMPO_DATA_CONCLUSIVA));
    }

    public static PropostaValidationContext complete(Proposta proposta, Clock clock) {
        Objects.requireNonNull(proposta);
        return new PropostaValidationContext(proposta, proposta.getValoriCampi(), true, null, clock);
    }

    public static PropostaValidationContext campoModificato(Proposta proposta,
                                                            Map<String, String> valoriCorrenti,
                                                            String nomeCampo,
                                                            String valore,
                                                            Clock clock) {
        Objects.requireNonNull(proposta);
        Objects.requireNonNull(nomeCampo);
        Map<String, String> merged = new LinkedHashMap<>(proposta.getValoriCampi());
        if (valoriCorrenti != null) {
            merged.putAll(valoriCorrenti);
        }
        merged.put(nomeCampo, valore);
        return new PropostaValidationContext(proposta, merged, false, nomeCampo, clock);
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
        if (complete) {
            return campi;
        }
        return campo(nomeCampoModificato)
                .map(List::of)
                .orElseGet(List::of);
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean deveValidareRelazioneTraCampi(String... nomiCampo) {
        return complete || Arrays.stream(nomiCampo).anyMatch(this::isCampoModificato);
    }

    public String valoreDi(String nomeCampo) {
        return valori.get(nomeCampo);
    }

    public LocalDate today() {
        return today;
    }

    public LocalDate subscriptionDeadline() {
        return subscriptionDeadline;
    }

    public LocalDate eventDate() {
        return eventDate;
    }

    public LocalDate closingDate() {
        return closingDate;
    }

    private boolean isCampoModificato(String nomeCampo) {
        return Objects.equals(nomeCampoModificato, nomeCampo);
    }

    private Optional<Campo> campo(String nomeCampo) {
        return campi.stream()
                .filter(campo -> campo.getNome().equals(nomeCampo))
                .findFirst();
    }
}
