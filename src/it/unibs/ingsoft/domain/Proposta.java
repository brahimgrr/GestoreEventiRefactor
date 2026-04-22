package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Proposta di iniziativa ricreativa.
 *
 * <p>Ciclo di vita: BOZZA → VALIDA → APERTA → CONFERMATA → CONCLUSA<br>
 *
 * <p>Invariante: le transizioni di stato rispettano la macchina a stati definita in
 * {@link StatoProposta}; ogni transizione viene registrata in {@code stateHistory}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Proposta {
    private final List<Campo> campiBase;
    private final List<Campo> campiComuni;
    private final Categoria categoria;
    private final Map<String, String> valoriCampi;
    private final List<String> listaAderenti;
    private final List<PropostaStateChange> stateHistory;
    private StatoProposta stato;
    private LocalDate dataPubblicazione;
    private LocalDate termineIscrizione;
    private LocalDate dataEvento;

    /**
     * Crea una nuova proposta in bozza.
     *
     * @pre categoria != null
     */
    public Proposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        if (categoria == null)
            throw new IllegalArgumentException("La categoria non può essere null.");
        this.categoria = new Categoria(categoria);
        this.campiBase = campiBase == null
                ? new ArrayList<>()
                : campiBase.stream().map(Campo::new).toList();
        this.campiComuni = campiComuni == null
                ? new ArrayList<>()
                : campiComuni.stream().map(Campo::new).toList();
        this.valoriCampi = new LinkedHashMap<>();
        this.listaAderenti = new ArrayList<>();
        this.stateHistory = new ArrayList<>();
        this.stato = StatoProposta.BOZZA;
        this.stateHistory.add(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)));
    }

    /**
     * Factory di deserializzazione Jackson — ricostruisce una proposta completamente popolata.
     */
    @JsonCreator
    public static Proposta fromJson(
            @JsonProperty("campiBase") List<Campo> campiBase,
            @JsonProperty("campiComuni") List<Campo> campiComuni,
            @JsonProperty("categoria") Categoria categoria,
            @JsonProperty("valoriCampi") Map<String, String> valoriCampi,
            @JsonProperty("stato") StatoProposta stato,
            @JsonProperty("dataPubblicazione") LocalDate dataPubblicazione,
            @JsonProperty("termineIscrizione") LocalDate termineIscrizione,
            @JsonProperty("dataEvento") LocalDate dataEvento,
            @JsonProperty("listaAderenti") List<String> listaAderenti,
            @JsonProperty("stateHistory") List<PropostaStateChange> stateHistory) {
        Proposta p = new Proposta(categoria, campiBase, campiComuni);
        if (valoriCampi != null) p.valoriCampi.putAll(valoriCampi);
        if (stato != null) p.stato = stato;
        if (dataPubblicazione != null) p.dataPubblicazione = dataPubblicazione;
        if (termineIscrizione != null) p.termineIscrizione = termineIscrizione;
        if (dataEvento != null) p.dataEvento = dataEvento;

        p.listaAderenti.clear();
        if (listaAderenti != null) p.listaAderenti.addAll(listaAderenti);

        if (stateHistory != null && !stateHistory.isEmpty()) {
            p.stateHistory.clear();
            p.stateHistory.addAll(stateHistory);
        }
        return p;
    }

    /**
     * Costruisce una chiave di identità case-insensitive (Titolo|Data|Ora|Luogo) dai valori grezzi.
     * Usata per il rilevamento duplicati prima che esista un oggetto {@code Proposta} (es. batch intra-file).
     */
    public static String chiaveIdentita(Map<String, String> valori) {
        return (valori.getOrDefault(AppConstants.CAMPO_TITOLO, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_DATA, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_ORA, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_LUOGO, "").trim()).toLowerCase();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @JsonIgnore
    public List<Campo> getCampi() {
        List<Campo> campiProposta = new ArrayList<>();
        campiProposta.addAll(campiBase);
        campiProposta.addAll(campiComuni);
        campiProposta.addAll(categoria.getCampiSpecifici());
        return campiProposta;
    }

    public StatoProposta getStato() {
        return stato;
    }

    /**
     * Esegue la transizione verso lo stato indicato e aggiorna {@code stateHistory}.
     *
     * @throws IllegalStateException se la transizione non è consentita dalla macchina a stati
     * @pre next != null
     */
    public void setStato(StatoProposta next) {
        if (next == null)
            throw new IllegalArgumentException("Stato non può essere null.");
        if (!stato.canTransitionTo(next))
            throw new IllegalStateException("Transizione non valida: " + stato + " → " + next + ".");
        this.stato = next;
        this.stateHistory.add(new PropostaStateChange(next, LocalDate.now(AppConstants.clock)));
    }

    public LocalDate getDataPubblicazione() {
        return dataPubblicazione;
    }

    /**
     * @pre d != null
     */
    public void setDataPubblicazione(LocalDate d) {
        this.dataPubblicazione = d;
    }

    public LocalDate getTermineIscrizione() {
        return termineIscrizione;
    }

    /**
     * @pre d != null
     */
    public void setTermineIscrizione(LocalDate d) {
        this.termineIscrizione = d;
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    /**
     * @pre d != null
     */
    public void setDataEvento(LocalDate d) {
        this.dataEvento = d;
    }

    public Map<String, String> getValoriCampi() {
        return Collections.unmodifiableMap(valoriCampi);
    }

    public List<String> getListaAderenti() {
        return Collections.unmodifiableList(listaAderenti);
    }

    /**
     * @pre stato == StatoProposta.APERTA
     * @pre username != null
     */
    public void addAderente(String username) {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Impossibile aggiungere aderenti: la proposta non è APERTA.");
        if (listaAderenti.contains(username))
            throw new IllegalStateException("L'utente è già iscritto a questa proposta.");
        if (isCapienzaRaggiunta())
            throw new IllegalStateException("Impossibile aggiungere aderenti: capienza massima raggiunta.");
        listaAderenti.add(username);
        // post condition: utente aggiunto a listaAderenti done.
    }

    /**
     * @pre stato == StatoProposta.APERTA
     */
    public void removeAderente(String username, LocalDate oggi) {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Impossibile rimuovere aderenti: la proposta non è APERTA.");
        if (isTermineIscrizioneScaduto(oggi))
            throw new IllegalStateException(
                    "Impossibile rimuovere: il termine di iscrizione è scaduto (" + termineIscrizione + ").");
        listaAderenti.remove(username);
    }

    /**
     * Riporta lo stato a BOZZA senza registrare il cambio in {@code stateHistory}.
     * Usato solo durante la validazione per evitare cicli BOZZA/VALIDA pre-pubblicazione.
     */
    public void revertToBozzaSilent() {
        if (this.stato == StatoProposta.VALIDA) {
            this.stato = StatoProposta.BOZZA;
        }
    }

    public List<PropostaStateChange> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    /**
     * Imposta i valori dei campi riordinandoli nell'ordine canonico
     * (base → comuni → specifici). Valori non mappati a nessun campo
     * noto vengono appesi in coda.
     */
    public void putAllValoriCampi(Map<String, String> valori) {
        if (stato != StatoProposta.BOZZA && stato != StatoProposta.VALIDA)
            throw new IllegalStateException(
                    "Impossibile modificare i campi di una proposta in stato " + stato + ".");
        valoriCampi.putAll(valori);

        Map<String, String> temp = new HashMap<>(valoriCampi);
        valoriCampi.clear();

        for (Campo c : getCampi()) {
            String nomeCampo = c.getNome();
            if (temp.containsKey(nomeCampo)) {
                valoriCampi.put(nomeCampo, temp.remove(nomeCampo));
            }
        }

        // Aggiunge eventuali campi legacy/extra rimanenti in coda
        valoriCampi.putAll(temp);
    }

    /**
     * Restituisce la data conclusiva effettiva: il campo facoltativo "Data conclusiva" se definito,
     * altrimenti {@link #getDataEvento()} come fallback.
     * Registra un avviso (stderr) se il campo è presente ma malformato.
     */
    @JsonIgnore
    public LocalDate getDataConclusiva() {
        String s = valoriCampi.get(AppConstants.CAMPO_DATA_CONCLUSIVA);
        if (s == null || s.isBlank()) return dataEvento;
        try {
            return LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
        } catch (DateTimeParseException e) {
            System.err.println("[WARN] Proposta: campo '" + AppConstants.CAMPO_DATA_CONCLUSIVA
                    + "' non parseable ('" + s + "'), fallback su dataEvento.");
            return dataEvento;
        }
    }

    /**
     * Restituisce il numero massimo di partecipanti dichiarato per questa proposta.
     *
     * @throws IllegalStateException se il campo è assente o non è un intero positivo
     */
    @JsonIgnore
    public int getNumeroPartecipanti() {
        String s = valoriCampi.get(AppConstants.CAMPO_NUM_PARTECIPANTI);
        if (s == null || s.isBlank())
            throw new IllegalStateException(
                    "Campo '" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non definito nella proposta.");
        try {
            int n = Integer.parseInt(s.trim());
            if (n <= 0)
                throw new IllegalStateException(
                        "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' deve essere un intero positivo.");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non è un intero valido: " + s);
        }
    }

    /**
     * Restituisce true quando l'elenco degli iscritti ha raggiunto la capacità massima.
     */
    @JsonIgnore
    public boolean isCapienzaRaggiunta() {
        return listaAderenti.size() >= getNumeroPartecipanti();
    }

    // ----------------------------------------------------------------
    // CHIAVE DI IDENTITÀ (rilevamento duplicati)
    // ----------------------------------------------------------------

    /**
     * Restituisce true quando la scadenza dell'iscrizione è già passata.
     */
    @JsonIgnore
    public boolean isTermineIscrizioneScaduto(LocalDate oggi) {
        return termineIscrizione != null && oggi.isAfter(termineIscrizione);
    }

    /**
     * Chiave di identità di questa proposta per il rilevamento duplicati.
     */
    @JsonIgnore
    public String getChiaveIdentita() {
        return chiaveIdentita(valoriCampi);
    }
}
