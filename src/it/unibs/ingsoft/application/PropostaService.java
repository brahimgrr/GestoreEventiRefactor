package it.unibs.ingsoft.application;

import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.persistence.api.IBachecaRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestisce il ciclo di vita della proposta: creazione, validazione, pubblicazione e query della bacheca.
 *
 * <h3>Transizioni di stato</h3>
 * <pre>  creaProposta()   → BOZZA
 *   validaProposta() → VALIDA (o rimane BOZZA in caso di errore)
 *   pubblicaProposta() → APERTA (persistita)</pre>
 *
 * <h3>Helper per le date</h3>
 * I tre metodi statici {@code is*Valido} sono l'unica fonte di verità per
 * i controlli dei limiti di data utilizzati da {@link #validaProposta}.
 */
public final class PropostaService {
    // ---- costanti dei nomi dei campi — alias di AppConstants (unica fonte di verità nel layer dominio) ----
    public static final String CAMPO_TITOLO = AppConstants.CAMPO_TITOLO;
    public static final String CAMPO_TERMINE_ISCRIZIONE = AppConstants.CAMPO_TERMINE_ISCRIZIONE;
    public static final String CAMPO_DATA = AppConstants.CAMPO_DATA;
    public static final String CAMPO_DATA_CONCLUSIVA = AppConstants.CAMPO_DATA_CONCLUSIVA;
    public static final String CAMPO_ORA = AppConstants.CAMPO_ORA;
    public static final String CAMPO_LUOGO = AppConstants.CAMPO_LUOGO;
    public static final String CAMPO_QUOTA = AppConstants.CAMPO_QUOTA;
    public static final String CAMPO_NUM_PARTECIPANTI = AppConstants.CAMPO_NUM_PARTECIPANTI;

    private final IBachecaRepository bachecaRepo;

    /**
     * Lista in memoria di proposte valide ma non pubblicate (ambito sessione, scartate al logout).
     */
    private final List<Proposta> proposteValide = new ArrayList<>();

    public PropostaService(IBachecaRepository bachecaRepo) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
    }

    /**
     * Vero quando {@code termine} è strettamente dopo oggi (come richiesto dalla specifica).
     */
    public static boolean isTermineIscrizioneValido(LocalDate termine) {
        return termine != null && termine.isAfter(LocalDate.now(AppConstants.clock));
    }

    /**
     * Vero quando {@code dataEvento} è almeno 2 giorni dopo {@code termine}.
     */
    public static boolean isDataEventoValida(LocalDate dataEvento, LocalDate termine) {
        return dataEvento != null && termine != null && dataEvento.isAfter(termine.plusDays(1));
    }

    /**
     * Vero quando {@code conclusiva} non è precedente a {@code data}.
     */
    public static boolean isDataConclusivaValida(LocalDate conclusiva, LocalDate data) {
        return conclusiva != null && data != null && !conclusiva.isBefore(data);
    }

    /**
     * Salva una proposta valida in memoria per la pubblicazione successiva.
     *
     * @throws IllegalStateException se esiste già una proposta con lo stesso Titolo, Data, Ora, Luogo
     *                               (pubblicata o in memoria)
     * @pre p.getStato() == StatoProposta.VALIDA
     */
    public void salvaProposta(Proposta p) {
        if (p.getStato() != StatoProposta.VALIDA)
            throw new IllegalStateException("Solo una proposta VALIDA può essere salvata.");
        rilevaDuplicatoAlSalvataggio(p);
        proposteValide.add(p);
    }

    /**
     * Restituisce la lista di proposte valide salvate in memoria (non ancora pubblicate).
     */
    public List<Proposta> getProposteValide() {
        return Collections.unmodifiableList(proposteValide);
    }

    // ----------------------------------------------------------------
    // HELPER STATICI PER DATE — riutilizzati dalla validazione a livello applicativo
    // ----------------------------------------------------------------

    /**
     * Rimuove una proposta dalla lista valida in memoria (ad es. dopo la pubblicazione).
     */
    public void rimuoviPropostaValida(Proposta p) {
        proposteValide.remove(p);
    }

    /**
     * Scarta tutte le proposte valide non pubblicate (chiamato al logout).
     */
    public void clearProposteValide() {
        proposteValide.clear();
    }

    private Bacheca bacheca() {
        return bachecaRepo.get();
    }

    // ----------------------------------------------------------------
    // CREAZIONE
    // ----------------------------------------------------------------

    /**
     * Crea una nuova proposta in bozza per la categoria indicata.
     *
     * @throws IllegalArgumentException se la categoria non esiste
     */
    public Proposta creaProposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        return new Proposta(categoria, campiBase, campiComuni);
    }

    // ----------------------------------------------------------------
    // VALIDAZIONE
    // ----------------------------------------------------------------

    /**
     * Valida la proposta rispetto ai vincoli dei campi obbligatori e delle date.
     *
     * <p>Idempotente: se la proposta è attualmente VALIDA, viene riportata a BOZZA
     * prima della ri-validazione in modo che lo stato rispecchi sempre il risultato più recente.</p>
     *
     * @return lista vuota al successo (proposta impostata su VALIDA);
     * lista di messaggi di errore al fallimento (proposta riportata a BOZZA)
     */
    public List<String> validaProposta(Proposta p) {
        // Riporta a BOZZA silenziosamente (nessuna voce nella cronologia) in modo che il metodo sia idempotente
        p.revertToBozzaSilent();

        List<String> errori = new ArrayList<>();
        Map<String, String> valori = p.getValoriCampi();
        Categoria cat = p.getCategoria();

        // 1. Campi obbligatori
        controllaCampiObbligatori(p.getCampi(), valori, errori);

        // 2. Numero di partecipanti deve essere un intero positivo
        String numStr = valori.get(CAMPO_NUM_PARTECIPANTI);
        if (numStr != null && !numStr.isBlank()) {
            try {
                int n = Integer.parseInt(numStr.trim());
                if (n <= 0)
                    errori.add("\"" + CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero positivo.");
            } catch (NumberFormatException e) {
                errori.add("\"" + CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero valido.");
            }
        }

        // 3. Vincoli sulle date
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null && !isTermineIscrizioneValido(termineIscr))
            errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna (" + oggi + ").");

        if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr))
            errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                    + CAMPO_TERMINE_ISCRIZIONE + "\".");

        if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento))
            errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");

        if (errori.isEmpty()) {
            p.setTermineIscrizione(termineIscr);
            p.setDataEvento(dataEvento);
            p.setStato(StatoProposta.VALIDA);
        }

        return errori;
    }

    /**
     * Valida un singolo campo nel contesto dei valori correnti della proposta.
     * Viene utilizzato durante l'inserimento interattivo per fornire feedback immediato sulle regole di business.
     */
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
                if (termineIscr != null && !isTermineIscrizioneValido(termineIscr))
                    errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna.");
                if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr))
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" +
                            CAMPO_TERMINE_ISCRIZIONE + "\".");
                break;

            case CAMPO_DATA:
                if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr))
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" +
                            CAMPO_TERMINE_ISCRIZIONE + "\".");
                if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento))
                    errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");
                break;

            case CAMPO_DATA_CONCLUSIVA:
                if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento))
                    errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");
                break;

            default:
                break;
        }

        return errori;
    }

    private void controllaCampiObbligatori(List<Campo> campi, Map<String, String> valori, List<String> errori) {
        for (Campo c : campi) {
            if (c.isObbligatorio()) {
                String v = valori.get(c.getNome());
                if (v == null || v.isBlank())
                    errori.add("Campo obbligatorio mancante: \"" + c.getNome() + "\".");
            }
        }
    }

    private LocalDate parseData(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    // ----------------------------------------------------------------
    // PUBBLICAZIONE
    // ----------------------------------------------------------------

    /**
     * Pubblica una proposta valida sulla bacheca (VALIDA → APERTA) e la persiste.
     *
     * @throws IllegalStateException se la proposta non è VALIDA, se la scadenza dell'iscrizione
     *                               è già passata, o se esiste un duplicato
     * @pre p.getStato() == StatoProposta.VALIDA
     */
    public void pubblicaProposta(Proposta p) {
        if (p.getStato() != StatoProposta.VALIDA)
            throw new IllegalStateException("La proposta deve essere in stato VALIDA per essere pubblicata.");

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        if (p.getTermineIscrizione() != null && !p.getTermineIscrizione().isAfter(oggi))
            throw new IllegalStateException(
                    "Non è più possibile pubblicare: il termine di iscrizione ("
                            + p.getTermineIscrizione() + ") è già scaduto. Rivalidare la proposta.");

        rilevaDuplicato(p);

        p.setStato(StatoProposta.APERTA);
        p.setDataPubblicazione(oggi);
        bacheca().addProposta(p);
        bachecaRepo.save();
    }

    /**
     * Controlla i duplicati al momento del salvataggio: rispetto sia alle proposte pubblicate (bacheca)
     * che alle proposte VALIDA in memoria. Questo protegge sia i flussi interattivi che batch.
     */
    private void rilevaDuplicatoAlSalvataggio(Proposta p) {
        String chiave = p.getChiaveIdentita();

        boolean inBacheca = bacheca().getProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));
        boolean inValide = proposteValide.stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (inBacheca || inValide)
            throw new IllegalStateException(
                    "Esiste già una proposta con lo stesso Titolo, Data, Ora e Luogo.");
    }

    /**
     * Controlla i duplicati al momento della pubblicazione: rispetto alle sole proposte pubblicate.
     */
    private void rilevaDuplicato(Proposta p) {
        String chiave = p.getChiaveIdentita();

        boolean duplicato = bacheca().getProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (duplicato)
            throw new IllegalStateException("Esiste già una proposta con lo stesso Titolo, Data, Ora e Luogo.");
    }

    // ----------------------------------------------------------------
    // BACHECA
    // ----------------------------------------------------------------

    /**
     * Restituisce tutte le proposte (in qualsiasi stato) come una lista piatta.
     */
    public List<Proposta> getTutteLeProposte() {
        return Collections.unmodifiableList(bacheca().getProposte());
    }

    /**
     * Restituisce tutte le proposte aperte (APERTA) come una lista piatta.
     */
    public List<Proposta> getBacheca() {
        return bacheca().getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce le proposte che il configuratore puo ritirare: APERTE e CONFERMATE.
     */
    public List<Proposta> getProposteRitirabili() {
        List<Proposta> ritirabili = new ArrayList<>(getBacheca());
        for (Proposta p : getTutteLeProposte()) {
            if (p.getStato() == StatoProposta.CONFERMATA && !ritirabili.contains(p)) {
                ritirabili.add(p);
            }
        }
        return Collections.unmodifiableList(ritirabili);
    }

    /**
     * Restituisce tutte le proposte (in qualsiasi stato) raggruppate per il loro stato attuale.
     */
    public Map<StatoProposta, List<Proposta>> getPropostePerStato() {
        Map<StatoProposta, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta p : getTutteLeProposte())
            mappa.computeIfAbsent(p.getStato(), k -> new ArrayList<>()).add(p);
        return mappa;
    }

    /**
     * Restituisce tutte le proposte aperte (APERTA), raggruppate per nome di categoria.
     */
    public Map<String, List<Proposta>> getBachecaPerCategoria() {
        Map<String, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta p : bacheca().getProposte()) {
            if (p.getStato() == StatoProposta.APERTA)
                mappa.computeIfAbsent(p.getCategoria().getNome(), k -> new ArrayList<>()).add(p);
        }
        return mappa;
    }

    /**
     * Restituisce il sottoinsieme di campi i cui nomi compaiono nell'elenco degli errori.
     *
     * <p>Utilizza la corrispondenza tra virgolette ({@code "\"nome\""}) in modo che un campo denominato
     * {@code "Data"} non sia falsamente abbinato da un messaggio di errore che
     * cita {@code "Data conclusiva"}.</p>
     */
    public List<Campo> getCampiConErrore(Proposta p, List<String> errori) {
        return p.getCampi().stream()
                .filter(c -> {
                    String quoted = "\"" + c.getNome() + "\"";
                    return errori.stream().anyMatch(e -> e.contains(quoted));
                })
                .collect(Collectors.toList());
    }

    /**
     * Applica i valori raccolti dalla UI e valida la proposta.
     */
    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        proposta.putAllValoriCampi(valori);
        List<String> errori = validaProposta(proposta);
        return new PropostaValidationResult(
                errori.isEmpty(),
                errori,
                getCampiConErrore(proposta, errori)
        );
    }

}
