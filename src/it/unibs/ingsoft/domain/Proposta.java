package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

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

    public Proposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        if (categoria == null)
            throw new IllegalArgumentException("La categoria non puo' essere null.");
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

    public static String chiaveIdentita(Map<String, String> valori) {
        return (valori.getOrDefault(AppConstants.CAMPO_TITOLO, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_DATA, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_ORA, "").trim() + "|"
                + valori.getOrDefault(AppConstants.CAMPO_LUOGO, "").trim()).toLowerCase();
    }

    public static boolean isTermineIscrizioneValido(LocalDate termine) {
        return termine != null && termine.isAfter(LocalDate.now(AppConstants.clock));
    }

    public static boolean isDataEventoValida(LocalDate dataEvento, LocalDate termine) {
        return dataEvento != null && termine != null && dataEvento.isAfter(termine.plusDays(1));
    }

    public static boolean isDataConclusivaValida(LocalDate conclusiva, LocalDate data) {
        return conclusiva != null && data != null && !conclusiva.isBefore(data);
    }

    private static LocalDate parseData(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
        } catch (Exception e) {
            return null;
        }
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

    private void cambiaStato(StatoProposta next) {
        if (next == null)
            throw new IllegalArgumentException("Stato non puo' essere null.");
        if (!stato.canTransitionTo(next))
            throw new IllegalStateException("Transizione non valida: " + stato + " → " + next + ".");
        this.stato = next;
        this.stateHistory.add(new PropostaStateChange(next, LocalDate.now(AppConstants.clock)));
    }

    public LocalDate getDataPubblicazione() {
        return dataPubblicazione;
    }

    private void setDataPubblicazione(LocalDate d) {
        this.dataPubblicazione = d;
    }

    public LocalDate getTermineIscrizione() {
        return termineIscrizione;
    }

    private void setTermineIscrizione(LocalDate d) {
        this.termineIscrizione = d;
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    private void setDataEvento(LocalDate d) {
        this.dataEvento = d;
    }

    public Map<String, String> getValoriCampi() {
        return Collections.unmodifiableMap(valoriCampi);
    }

    public List<String> getListaAderenti() {
        return Collections.unmodifiableList(listaAderenti);
    }

    @JsonIgnore
    public boolean isValida() {
        return stato == StatoProposta.VALIDA;
    }

    @JsonIgnore
    public boolean isAperta() {
        return stato == StatoProposta.APERTA;
    }

    @JsonIgnore
    public boolean isConfermata() {
        return stato == StatoProposta.CONFERMATA;
    }

    @JsonIgnore
    public boolean isRitirabile() {
        return isAperta() || isConfermata();
    }

    @JsonIgnore
    public boolean isIscritto(String username) {
        return listaAderenti.contains(username);
    }

    public String valoreCampoOrDefault(String nomeCampo, String defaultValue) {
        return valoriCampi.getOrDefault(nomeCampo, defaultValue);
    }

    public void verificaSalvabile() {
        if (!isValida()) {
            throw new IllegalStateException("Solo una proposta VALIDA puo' essere salvata.");
        }
    }

    public void verificaPubblicabile(LocalDate oggi) {
        if (!isValida()) {
            throw new IllegalStateException("La proposta deve essere in stato VALIDA per essere pubblicata.");
        }

        if (termineIscrizione != null && !termineIscrizione.isAfter(oggi)) {
            throw new IllegalStateException(
                    "Non e' piu' possibile pubblicare: il termine di iscrizione ("
                            + termineIscrizione + ") E' gia' scaduto. Rivalidare la proposta.");
        }
    }

    public void pubblica(LocalDate oggi) {
        verificaPubblicabile(oggi);
        cambiaStato(StatoProposta.APERTA);
        setDataPubblicazione(oggi);
    }

    public boolean confermaSeAperta() {
        if (!isAperta()) return false;
        cambiaStato(StatoProposta.CONFERMATA);
        return true;
    }

    public boolean annullaSeAperta() {
        if (!isAperta()) return false;
        cambiaStato(StatoProposta.ANNULLATA);
        return true;
    }

    public boolean concludiSeConfermata() {
        if (!isConfermata()) return false;
        cambiaStato(StatoProposta.CONCLUSA);
        return true;
    }

    public void ritira(LocalDate oggi) {
        if (!isRitirabile()) {
            throw new IllegalStateException(
                    "Impossibile ritirare: la proposta non e' APERTA ne' CONFERMATA.");
        }

        if (dataEvento != null && !oggi.isBefore(dataEvento)) {
            throw new IllegalStateException(
                    "Impossibile ritirare: il ritiro e' consentito solo entro il giorno precedente la data dell'evento.");
        }

        cambiaStato(StatoProposta.RITIRATA);
    }

    public EsitoTransizioneProposta applicaTransizionePerScadenza(LocalDate oggi) {
        if (deveChiudereIscrizioni(oggi)) {
            if (haNumeroPartecipantiCompleto() && confermaSeAperta()) {
                return EsitoTransizioneProposta.CONFERMATA;
            }

            if (annullaSeAperta()) {
                return EsitoTransizioneProposta.ANNULLATA;
            }
        } else if (deveConcludersi(oggi) && concludiSeConfermata()) {
            return EsitoTransizioneProposta.CONCLUSA;
        }

        return EsitoTransizioneProposta.NESSUNA;
    }

    @JsonIgnore
    public boolean deveChiudereIscrizioni(LocalDate oggi) {
        return isAperta() && isTermineIscrizioneScaduto(oggi);
    }

    @JsonIgnore
    public boolean deveConcludersi(LocalDate oggi) {
        LocalDate conclusiva = getDataConclusivaSilenziosa();
        return isConfermata() && conclusiva != null && oggi.isAfter(conclusiva);
    }

    @JsonIgnore
    public boolean haNumeroPartecipantiCompleto() {
        return listaAderenti.size() == getNumeroPartecipanti();
    }

    public void iscrivi(String username, LocalDate oggi) {
        if (!isAperta()) {
            throw new IllegalStateException("Impossibile iscriversi: la proposta non e' APERTA.");
        }

        if (isTermineIscrizioneScaduto(oggi)) {
            throw new IllegalStateException(
                    "Impossibile iscriversi: il termine di iscrizione e' scaduto (" + termineIscrizione + ").");
        }

        if (isIscritto(username)) {
            throw new IllegalStateException("Sei gia' iscritto a questa proposta.");
        }

        int numeroPartecipantiPrevisto = getNumeroPartecipanti();
        if (listaAderenti.size() >= numeroPartecipantiPrevisto) {
            throw new IllegalStateException(
                    "Impossibile iscriversi: la proposta ha già raggiunto il numero massimo di partecipanti.");
        }

        listaAderenti.add(username);
    }

    public void disiscrivi(String username, LocalDate oggi) {
        if (!isAperta()) {
            throw new IllegalStateException("Impossibile disdire: la proposta non e' APERTA.");
        }

        if (isTermineIscrizioneScaduto(oggi)) {
            throw new IllegalStateException(
                    "Impossibile disdire: il termine di iscrizione e' scaduto (" + termineIscrizione + ").");
        }

        if (!isIscritto(username)) {
            throw new IllegalStateException("Non sei iscritto a questa proposta.");
        }

        listaAderenti.remove(username);
    }

    public void addAderente(String username) {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Impossibile aggiungere aderenti: la proposta non e' APERTA.");
        if (listaAderenti.contains(username))
            throw new IllegalStateException("L'utente e' gia' iscritto a questa proposta.");
        if (isCapienzaRaggiunta())
            throw new IllegalStateException("Impossibile aggiungere aderenti: capienza massima raggiunta.");
        listaAderenti.add(username);
        // post condition: utente aggiunto a listaAderenti done.
    }

    public void removeAderente(String username, LocalDate oggi) {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Impossibile rimuovere aderenti: la proposta non e' APERTA.");
        if (isTermineIscrizioneScaduto(oggi))
            throw new IllegalStateException(
                    "Impossibile rimuovere: il termine di iscrizione e' scaduto (" + termineIscrizione + ").");
        listaAderenti.remove(username);
    }

    public void revertToBozzaSilent() {
        if (this.stato == StatoProposta.VALIDA) {
            this.stato = StatoProposta.BOZZA;
        }
    }

    public List<PropostaStateChange> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    public void aggiornaValoriCampi(Map<String, String> valori) {
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

    public void putAllValoriCampi(Map<String, String> valori) {
        aggiornaValoriCampi(valori);
    }

    public List<String> valida() {
        revertToBozzaSilent();

        List<String> errori = new ArrayList<>();
        controllaCampiObbligatori(errori);
        controllaNumeroPartecipanti(errori);

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        LocalDate termineIscr = parseData(valoriCampi.get(AppConstants.CAMPO_TERMINE_ISCRIZIONE));
        LocalDate data = parseData(valoriCampi.get(AppConstants.CAMPO_DATA));
        LocalDate dataConclusiva = parseData(valoriCampi.get(AppConstants.CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null && !isTermineIscrizioneValido(termineIscr)) {
            errori.add("\"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE
                    + "\" deve essere successivo alla data odierna (" + oggi + ").");
        }

        if (termineIscr != null && data != null && !isDataEventoValida(data, termineIscr)) {
            errori.add("\"" + AppConstants.CAMPO_DATA
                    + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                    + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\".");
        }

        if (data != null && dataConclusiva != null && !isDataConclusivaValida(dataConclusiva, data)) {
            errori.add("\"" + AppConstants.CAMPO_DATA_CONCLUSIVA
                    + "\" non puo' essere precedente a \"" + AppConstants.CAMPO_DATA + "\".");
        }

        if (errori.isEmpty()) {
            setTermineIscrizione(termineIscr);
            setDataEvento(data);
            cambiaStato(StatoProposta.VALIDA);
        }

        return errori;
    }

    public List<String> validaCampo(Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        Map<String, String> valori = new LinkedHashMap<>(valoriCampi);
        valori.putAll(valoriCorrenti);
        valori.put(nomeCampo, valore);

        List<String> errori = new ArrayList<>();
        LocalDate termineIscr = parseData(valori.get(AppConstants.CAMPO_TERMINE_ISCRIZIONE));
        LocalDate data = parseData(valori.get(AppConstants.CAMPO_DATA));
        LocalDate dataConclusiva = parseData(valori.get(AppConstants.CAMPO_DATA_CONCLUSIVA));

        switch (nomeCampo) {
            case AppConstants.CAMPO_TERMINE_ISCRIZIONE:
                if (termineIscr != null && !isTermineIscrizioneValido(termineIscr)) {
                    errori.add("\"" + AppConstants.CAMPO_TERMINE_ISCRIZIONE
                            + "\" deve essere successivo alla data odierna.");
                }
                if (termineIscr != null && data != null && !isDataEventoValida(data, termineIscr)) {
                    errori.add("\"" + AppConstants.CAMPO_DATA
                            + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                            + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\".");
                }
                break;

            case AppConstants.CAMPO_DATA:
                if (termineIscr != null && data != null && !isDataEventoValida(data, termineIscr)) {
                    errori.add("\"" + AppConstants.CAMPO_DATA
                            + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                            + AppConstants.CAMPO_TERMINE_ISCRIZIONE + "\".");
                }
                if (data != null && dataConclusiva != null && !isDataConclusivaValida(dataConclusiva, data)) {
                    errori.add("\"" + AppConstants.CAMPO_DATA_CONCLUSIVA
                            + "\" non puo' essere precedente a \"" + AppConstants.CAMPO_DATA + "\".");
                }
                break;

            case AppConstants.CAMPO_DATA_CONCLUSIVA:
                if (data != null && dataConclusiva != null && !isDataConclusivaValida(dataConclusiva, data)) {
                    errori.add("\"" + AppConstants.CAMPO_DATA_CONCLUSIVA
                            + "\" non puo' essere precedente a \"" + AppConstants.CAMPO_DATA + "\".");
                }
                break;

            default:
                break;
        }

        return errori;
    }

    private void controllaCampiObbligatori(List<String> errori) {
        for (Campo campo : getCampi()) {
            if (campo.isObbligatorio()) {
                String valore = valoriCampi.get(campo.getNome());
                if (valore == null || valore.isBlank()) {
                    errori.add("Campo obbligatorio mancante: \"" + campo.getNome() + "\".");
                }
            }
        }
    }

    private void controllaNumeroPartecipanti(List<String> errori) {
        String numStr = valoriCampi.get(AppConstants.CAMPO_NUM_PARTECIPANTI);
        if (numStr == null || numStr.isBlank()) {
            return;
        }

        try {
            int n = Integer.parseInt(numStr.trim());
            if (n <= 0) {
                errori.add("\"" + AppConstants.CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero positivo.");
            }
        } catch (NumberFormatException e) {
            errori.add("\"" + AppConstants.CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero valido.");
        }
    }

    @JsonIgnore
    public LocalDate getDataConclusiva() {
        return getDataConclusiva(true);
    }

    private LocalDate getDataConclusivaSilenziosa() {
        return getDataConclusiva(false);
    }

    private LocalDate getDataConclusiva(boolean avvisaSeMalformata) {
        String s = valoriCampi.get(AppConstants.CAMPO_DATA_CONCLUSIVA);
        if (s == null || s.isBlank()) return dataEvento;
        try {
            return LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
        } catch (DateTimeParseException e) {
            if (avvisaSeMalformata) {
                System.err.println("[WARN] Proposta: campo '" + AppConstants.CAMPO_DATA_CONCLUSIVA
                        + "' non parseable ('" + s + "'), fallback su dataEvento.");
            }
            return dataEvento;
        }
    }

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
                    "'" + AppConstants.CAMPO_NUM_PARTECIPANTI + "' non e' un intero valido: " + s);
        }
    }

    @JsonIgnore
    public boolean isCapienzaRaggiunta() {
        return listaAderenti.size() >= getNumeroPartecipanti();
    }

    // ----------------------------------------------------------------
    // CHIAVE DI IDENTITÀ (rilevamento duplicati)
    // ----------------------------------------------------------------

    @JsonIgnore
    public boolean isTermineIscrizioneScaduto(LocalDate oggi) {
        return termineIscrizione != null && oggi.isAfter(termineIscrizione);
    }

    @JsonIgnore
    public String getChiaveIdentita() {
        return chiaveIdentita(valoriCampi);
    }
}
