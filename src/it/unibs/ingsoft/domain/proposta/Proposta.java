package it.unibs.ingsoft.domain.proposta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.shared.AppConstants;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Proposta {
    private final String id;
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
        this(UUID.randomUUID().toString(), categoria, campiBase, campiComuni);
    }

    private Proposta(String id, Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        if (categoria == null)
            throw new DomainException(DomainErrorCode.NULL_PROPOSTA_CATEGORY);
        this.id = normalizzaId(id);
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
            @JsonProperty("id") String id,
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
        Proposta p = new Proposta(id, categoria, campiBase, campiComuni);
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

    private static String normalizzaId(String id) {
        return (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id.trim();
    }

    public String getId() {
        return id;
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
        LocalDate conclusiva = getDataConclusivaOrDataEvento();
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

    public void applicaEsitoValidazione(PropostaValidationOutcome outcome) {
        Objects.requireNonNull(outcome);
        if (outcome.valida()) {
            segnaValidata(outcome.termineIscrizione(), outcome.dataEvento());
        } else {
            riportaInBozzaSeValida();
        }
    }

    private void riportaInBozzaSeValida() {
        if (this.stato == StatoProposta.VALIDA) {
            cambiaStato(StatoProposta.BOZZA);
        }
    }

    private void segnaValidata(LocalDate termineIscrizione, LocalDate dataEvento) {
        setTermineIscrizione(termineIscrizione);
        setDataEvento(dataEvento);
        if (stato == StatoProposta.BOZZA) {
            cambiaStato(StatoProposta.VALIDA);
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

    @JsonIgnore
    public LocalDate getDataConclusiva() {
        return getDataConclusivaOrDataEvento();
    }

    private LocalDate getDataConclusivaOrDataEvento() {
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

    @JsonIgnore
    public boolean isTermineIscrizioneScaduto(LocalDate oggi) {
        return termineIscrizione != null && oggi.isAfter(termineIscrizione);
    }
}
