package it.unibs.ingsoft.domain.model.proposta;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.AppConstants;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

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
            throw new DomainException(new ProposalFailure.NullCategory());
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

    /**
     * Ricostruisce una proposta da stato persistito gia' validato dal repository.
     * Non e' legato a JSON: vale anche per file, database o test fixtures.
     */
    public static Proposta rehydrate(
            String id,
            List<Campo> campiBase,
            List<Campo> campiComuni,
            Categoria categoria,
            Map<String, String> valoriCampi,
            StatoProposta stato,
            LocalDate dataPubblicazione,
            LocalDate termineIscrizione,
            LocalDate dataEvento,
            List<String> listaAderenti,
            List<PropostaStateChange> stateHistory) {
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
            throw new DomainException(new ProposalFailure.NullState());
        if (!stato.canTransitionTo(next))
            throw new DomainException(new ProposalFailure.InvalidStateTransition(stato, next));
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

    public List<Campo> getCampiBase() {
        return Collections.unmodifiableList(campiBase);
    }

    public List<Campo> getCampiComuni() {
        return Collections.unmodifiableList(campiComuni);
    }

    public boolean isValida() {
        return stato == StatoProposta.VALIDA;
    }

    public boolean isAperta() {
        return stato == StatoProposta.APERTA;
    }

    public boolean isConfermata() {
        return stato == StatoProposta.CONFERMATA;
    }

    public boolean isRitirabile() {
        return isAperta() || isConfermata();
    }

    public boolean isIscritto(String username) {
        return listaAderenti.contains(username);
    }

    public String valoreCampoOrDefault(String nomeCampo, String defaultValue) {
        return valoriCampi.getOrDefault(nomeCampo, defaultValue);
    }

    public static String chiaveIdentita(Map<String, String> valori) {
        return PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(valori);
    }

    public String getChiaveIdentita() {
        return chiaveIdentita(valoriCampi);
    }

    public void verificaSalvabile() {
        if (!isValida()) {
            throw new DomainException(new ProposalFailure.NotSavable());
        }
    }

    public void verificaPubblicabile(LocalDate oggi) {
        if (!isValida()) {
            throw new DomainException(new ProposalFailure.NotValidForPublication());
        }

        if (termineIscrizione != null && !termineIscrizione.isAfter(oggi)) {
            throw new DomainException(new ProposalFailure.PublicationDeadlineExpired(termineIscrizione));
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
            throw new DomainException(new ProposalFailure.NotWithdrawable());
        }

        if (dataEvento != null && !oggi.isBefore(dataEvento)) {
            throw new DomainException(new ProposalFailure.WithdrawalTooLate());
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

    public boolean deveChiudereIscrizioni(LocalDate oggi) {
        return isAperta() && isTermineIscrizioneScaduto(oggi);
    }

    public boolean deveConcludersi(LocalDate oggi) {
        LocalDate conclusiva = getDataConclusivaOrDataEvento();
        return isConfermata() && conclusiva != null && oggi.isAfter(conclusiva);
    }

    public boolean haNumeroPartecipantiCompleto() {
        return listaAderenti.size() == getNumeroPartecipanti();
    }

    public void iscrivi(String username, LocalDate oggi) {
        if (!isAperta()) {
            throw new DomainException(new ProposalFailure.NotOpenForSubscription());
        }
        if (isTermineIscrizioneScaduto(oggi)) {
            throw new DomainException(new ProposalFailure.SubscriptionDeadlineExpired(termineIscrizione));
        }
        if (isIscritto(username)) {
            throw new DomainException(new ProposalFailure.AlreadySubscribed());
        }
        int numeroPartecipantiPrevisto = getNumeroPartecipanti();
        if (listaAderenti.size() >= numeroPartecipantiPrevisto) {
            throw new DomainException(new ProposalFailure.Full());
        }

        listaAderenti.add(username);
    }

    public void disiscrivi(String username, LocalDate oggi) {
        if (!isAperta()) {
            throw new DomainException(new ProposalFailure.NotOpenForUnsubscription());
        }

        if (isTermineIscrizioneScaduto(oggi)) {
            throw new DomainException(new ProposalFailure.UnsubscriptionDeadlineExpired(termineIscrizione));
        }

        if (!isIscritto(username)) {
            throw new DomainException(new ProposalFailure.NotSubscribed());
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

    public void valida() {
        applicaEsitoValidazione(new PropostaValidator().validaCompleta(this));
    }

    private void riportaInBozzaSeValida() {
        if (this.stato == StatoProposta.VALIDA) {
            this.stato = StatoProposta.BOZZA;
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
            throw new DomainException(new ProposalFailure.FieldsNotModifiable(stato));
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

    public int getNumeroPartecipanti() {
        String s = valoriCampi.get(AppConstants.CAMPO_NUM_PARTECIPANTI);
        if (s == null || s.isBlank())
            throw new DomainException(new ProposalFailure.ParticipantsMissing());
        try {
            int n = Integer.parseInt(s.trim());
            if (n <= 0)
                throw new DomainException(new ProposalFailure.ParticipantsNotPositive());
            return n;
        } catch (NumberFormatException e) {
            throw new DomainException(new ProposalFailure.ParticipantsNotInteger(s));
        }
    }

    public boolean isCapienzaRaggiunta() {
        return listaAderenti.size() >= getNumeroPartecipanti();
    }

    public boolean isTermineIscrizioneScaduto(LocalDate oggi) {
        return termineIscrizione != null && oggi.isAfter(termineIscrizione);
    }
}
