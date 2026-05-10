package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.validation.ValidationError;
import it.unibs.ingsoft.domain.validation.ValidationErrorCode;

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
            throw new DomainException(DomainErrorCode.NULL_PROPOSTA_CATEGORY);
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
            throw new DomainException(DomainErrorCode.NULL_STATO_PROPOSTA);
        if (!stato.canTransitionTo(next))
            throw DomainException.invalidStateTransition(stato, next);
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
            throw new DomainException(DomainErrorCode.PROPOSTA_NOT_SALVABILE);
        }
    }

    public void verificaPubblicabile(LocalDate oggi) {
        if (!isValida()) {
            throw new DomainException(DomainErrorCode.PROPOSTA_NOT_VALID_FOR_PUBLICATION);
        }

        if (termineIscrizione != null && !termineIscrizione.isAfter(oggi)) {
            throw DomainException.publicationDeadlineExpired(termineIscrizione);
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
            throw new DomainException(DomainErrorCode.PROPOSTA_NOT_WITHDRAWABLE);
        }

        if (dataEvento != null && !oggi.isBefore(dataEvento)) {
            throw new DomainException(DomainErrorCode.PROPOSTA_WITHDRAWAL_TOO_LATE);
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
            throw new DomainException(DomainErrorCode.PROPOSTA_NOT_OPEN_FOR_SUBSCRIPTION);
        }

        if (isTermineIscrizioneScaduto(oggi)) {
            throw DomainException.subscriptionDeadlineExpired(termineIscrizione);
        }

        if (isIscritto(username)) {
            throw new DomainException(DomainErrorCode.PROPOSTA_ALREADY_SUBSCRIBED);
        }

        int numeroPartecipantiPrevisto = getNumeroPartecipanti();
        if (listaAderenti.size() >= numeroPartecipantiPrevisto) {
            throw new DomainException(DomainErrorCode.PROPOSTA_FULL);
        }

        listaAderenti.add(username);
    }

    public void disiscrivi(String username, LocalDate oggi) {
        if (!isAperta()) {
            throw new DomainException(DomainErrorCode.PROPOSTA_NOT_OPEN_FOR_UNSUBSCRIPTION);
        }

        if (isTermineIscrizioneScaduto(oggi)) {
            throw DomainException.unsubscriptionDeadlineExpired(termineIscrizione);
        }

        if (!isIscritto(username)) {
            throw new DomainException(DomainErrorCode.PROPOSTA_NOT_SUBSCRIBED);
        }

        listaAderenti.remove(username);
    }

    private void revertToBozzaSilent() {
        if (this.stato == StatoProposta.VALIDA) {
            this.stato = StatoProposta.BOZZA;
        }
    }

    public List<PropostaStateChange> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    public void aggiornaValoriCampi(Map<String, String> valori) {
        if (stato != StatoProposta.BOZZA && stato != StatoProposta.VALIDA)
            throw DomainException.fieldsNotModifiable(stato);
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

    public List<ValidationError> valida() {
        revertToBozzaSilent();

        List<ValidationError> errori = new ArrayList<>();
        controllaCampiObbligatori(errori);
        controllaNumeroPartecipanti(errori);

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        LocalDate termineIscr = parseData(valoriCampi.get(AppConstants.CAMPO_TERMINE_ISCRIZIONE));
        LocalDate data = parseData(valoriCampi.get(AppConstants.CAMPO_DATA));
        LocalDate dataConclusiva = parseData(valoriCampi.get(AppConstants.CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null && !isTermineIscrizioneValido(termineIscr)) {
            errori.add(ValidationError.termineIscrizioneNonFuturo(
                    AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                    oggi));
        }

        if (termineIscr != null && data != null && !isDataEventoValida(data, termineIscr)) {
            errori.add(ValidationError.error(
                    AppConstants.CAMPO_DATA,
                    ValidationErrorCode.DATA_EVENTO_TROPPO_PRESTO));
        }

        if (data != null && dataConclusiva != null && !isDataConclusivaValida(dataConclusiva, data)) {
            errori.add(ValidationError.error(
                    AppConstants.CAMPO_DATA_CONCLUSIVA,
                    ValidationErrorCode.DATA_CONCLUSIVA_PRECEDENTE));
        }

        if (errori.isEmpty()) {
            setTermineIscrizione(termineIscr);
            setDataEvento(data);
            cambiaStato(StatoProposta.VALIDA);
        }

        return errori;
    }

    public List<ValidationError> validaCampo(Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        Map<String, String> valori = new LinkedHashMap<>(valoriCampi);
        valori.putAll(valoriCorrenti);
        valori.put(nomeCampo, valore);

        List<ValidationError> errori = new ArrayList<>();
        LocalDate termineIscr = parseData(valori.get(AppConstants.CAMPO_TERMINE_ISCRIZIONE));
        LocalDate data = parseData(valori.get(AppConstants.CAMPO_DATA));
        LocalDate dataConclusiva = parseData(valori.get(AppConstants.CAMPO_DATA_CONCLUSIVA));

        switch (nomeCampo) {
            case AppConstants.CAMPO_TERMINE_ISCRIZIONE:
                if (termineIscr != null && !isTermineIscrizioneValido(termineIscr)) {
                    errori.add(ValidationError.error(
                            AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                            ValidationErrorCode.TERMINE_ISCRIZIONE_NON_FUTURO));
                }
                if (termineIscr != null && data != null && !isDataEventoValida(data, termineIscr)) {
                    errori.add(ValidationError.error(
                            AppConstants.CAMPO_DATA,
                            ValidationErrorCode.DATA_EVENTO_TROPPO_PRESTO));
                }
                break;

            case AppConstants.CAMPO_DATA:
                if (termineIscr != null && data != null && !isDataEventoValida(data, termineIscr)) {
                    errori.add(ValidationError.error(
                            AppConstants.CAMPO_DATA,
                            ValidationErrorCode.DATA_EVENTO_TROPPO_PRESTO));
                }
                if (data != null && dataConclusiva != null && !isDataConclusivaValida(dataConclusiva, data)) {
                    errori.add(ValidationError.error(
                            AppConstants.CAMPO_DATA_CONCLUSIVA,
                            ValidationErrorCode.DATA_CONCLUSIVA_PRECEDENTE));
                }
                break;

            case AppConstants.CAMPO_DATA_CONCLUSIVA:
                if (data != null && dataConclusiva != null && !isDataConclusivaValida(dataConclusiva, data)) {
                    errori.add(ValidationError.error(
                            AppConstants.CAMPO_DATA_CONCLUSIVA,
                            ValidationErrorCode.DATA_CONCLUSIVA_PRECEDENTE));
                }
                break;

            default:
                break;
        }

        return errori;
    }

    private void controllaCampiObbligatori(List<ValidationError> errori) {
        for (Campo campo : getCampi()) {
            if (campo.isObbligatorio()) {
                String valore = valoriCampi.get(campo.getNome());
                if (valore == null || valore.isBlank()) {
                    errori.add(ValidationError.error(
                            campo.getNome(),
                            ValidationErrorCode.CAMPO_OBBLIGATORIO_MANCANTE));
                }
            }
        }
    }

    private void controllaNumeroPartecipanti(List<ValidationError> errori) {
        String numStr = valoriCampi.get(AppConstants.CAMPO_NUM_PARTECIPANTI);
        if (numStr == null || numStr.isBlank()) {
            return;
        }

        try {
            int n = Integer.parseInt(numStr.trim());
            if (n <= 0) {
                errori.add(ValidationError.error(
                        AppConstants.CAMPO_NUM_PARTECIPANTI,
                        ValidationErrorCode.NUMERO_PARTECIPANTI_NON_POSITIVO));
            }
        } catch (NumberFormatException e) {
            errori.add(ValidationError.error(
                    AppConstants.CAMPO_NUM_PARTECIPANTI,
                    ValidationErrorCode.NUMERO_PARTECIPANTI_NON_INTERO));
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
            return dataEvento;
        }
    }

    @JsonIgnore
    public int getNumeroPartecipanti() {
        String s = valoriCampi.get(AppConstants.CAMPO_NUM_PARTECIPANTI);
        if (s == null || s.isBlank())
            throw new DomainException(DomainErrorCode.PROPOSTA_PARTICIPANTS_MISSING);
        try {
            int n = Integer.parseInt(s.trim());
            if (n <= 0)
                throw new DomainException(DomainErrorCode.PROPOSTA_PARTICIPANTS_NOT_POSITIVE);
            return n;
        } catch (NumberFormatException e) {
            throw DomainException.participantsNotInteger(s);
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
