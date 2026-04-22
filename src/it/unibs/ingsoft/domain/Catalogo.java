package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Catalogo dei campi e delle categorie dell'applicazione.
 * Le mutazioni strutturali sono mantenute qui; la logica di business risiede nel service layer.
 *
 * <p>Invariante: una volta che {@code campiBaseFissati == true}, {@code campiBase}
 * non viene mai più modificato. I nomi dei campi sono univoci globalmente
 * (case-insensitive) tra campi base e campi comuni.</p>
 */
public final class Catalogo {
    private final List<Campo> campiBase = new ArrayList<>();
    private final List<Campo> campiComuni = new ArrayList<>();
    private final List<Categoria> categorie = new ArrayList<>();

    private boolean campiBaseFissati;

    /**
     * Factory di deserializzazione Jackson.
     */
    @JsonCreator
    public static Catalogo fromJson(
            @JsonProperty("campiBase") List<Campo> campiBase,
            @JsonProperty("campiBaseFissati") boolean campiBaseFissati,
            @JsonProperty("campiComuni") List<Campo> campiComuni,
            @JsonProperty("categorie") List<Categoria> categorie
    ) {
        Catalogo d = new Catalogo();
        if (campiBase != null) d.campiBase.addAll(campiBase);
        if (campiBaseFissati) d.campiBaseFissati = true;
        if (campiComuni != null) d.campiComuni.addAll(campiComuni);
        if (categorie != null) d.categorie.addAll(categorie);
        return d;
    }
    // ---------------- CAMPI BASE ----------------

    /**
     * Fissa i campi base (fissi + eventuali extra). Può essere invocato una sola volta.
     *
     * @throws IllegalStateException    se i campi base sono già stati fissati
     * @throws IllegalArgumentException se un campo è duplicato o già esistente globalmente
     * @pre !campiBaseFissati
     * @pre base != null
     * @post campiBaseFissati == true
     */
    public void fissareCampiBase(List<Campo> base, List<Campo> extra) {
        if (campiBaseFissati)
            throw new IllegalStateException("Campi base già fissati.");

        campiBase.clear();

        Set<String> nomi = new HashSet<>();

        for (Campo c : base) {
            addNomeUnico(nomi, c.getNome());
            campiBase.add(c);
        }

        if (extra != null) {
            for (Campo c : extra) {
                if (c == null || c.getNome().isBlank()) continue;

                addNomeUnico(nomi, c.getNome());

                if (nomeEsistenteGlobale(c.getNome()))
                    throw new IllegalArgumentException("Campo già esistente: " + c.getNome());

                campiBase.add(c);
            }
        }

        campiBaseFissati = true;
    }

    private void addNomeUnico(Set<String> set, String nome) {
        if (!set.add(nome.toLowerCase()))
            throw new IllegalArgumentException("Duplicato: " + nome);
    }

    public boolean isCampiBaseFissati() {
        return campiBaseFissati;
    }

    // ---------------- CAMPI COMUNI ----------------

    /**
     * @throws IllegalArgumentException se il nome è già presente globalmente
     * @pre campo != null
     */
    public void addCampoComune(Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome()))
            throw new IllegalArgumentException("Campo già esistente.");

        campiComuni.add(campo);
    }

    /**
     * @return {@code true} se rimosso, {@code false} se non trovato
     */
    public boolean removeCampoComune(String nome) {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    /**
     * Aggiorna il flag {@code obbligatorio} del campo comune indicato.
     *
     * @return {@code true} se aggiornato, {@code false} se non trovato
     */
    public boolean updateCampoComune(String nome, boolean obbligatorio) {
        for (int i = 0; i < campiComuni.size(); i++) {
            Campo c = campiComuni.get(i);
            if (c.getNome().equalsIgnoreCase(nome)) {
                campiComuni.set(i, c.withObbligatorio(obbligatorio));
                return true;
            }
        }
        return false;
    }

    // ---------------- CATEGORIE ----------------

    /**
     * @throws IllegalArgumentException se la categoria esiste già
     * @pre nome != null &amp;&amp; !nome.isBlank()
     * @post getCategorie().stream().anyMatch(c - > c.getNome ().equalsIgnoreCase(nome))
     */
    public Categoria addCategoria(String nome) {
        if (findCategoria(nome).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria c = new Categoria(nome);
        categorie.add(c);
        return c;
    }

    /**
     * @return {@code true} se rimossa, {@code false} se non trovata
     */
    public boolean removeCategoria(String nome) {
        return categorie.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    public Categoria getCategoriaOrThrow(String nome) {
        return findCategoria(nome)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata."));
    }

    private Optional<Categoria> findCategoria(String nome) {
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    // ---------------- CAMPI SPECIFICI ----------------

    /**
     * @throws IllegalArgumentException se il nome è già presente globalmente o la categoria non esiste
     * @pre campo.getTipo() == TipoCampo.SPECIFICO
     */
    public void addCampoSpecifico(String categoria, Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome()))
            throw new IllegalArgumentException("Campo già esistente.");

        getCategoriaOrThrow(categoria).addCampoSpecifico(campo);
    }

    /**
     * @return {@code true} se rimosso, {@code false} se non trovato
     * @throws IllegalArgumentException se la categoria non esiste
     */
    public boolean removeCampoSpecifico(String categoria, String nome) {
        return getCategoriaOrThrow(categoria).removeCampoSpecifico(nome);
    }

    /**
     * @return {@code true} se aggiornato, {@code false} se non trovato
     * @throws IllegalArgumentException se la categoria non esiste
     */
    public boolean updateCampoSpecifico(String categoria, String nome, boolean obbligatorio) {
        return getCategoriaOrThrow(categoria)
                .setObbligatorietaCampoSpecifico(nome, obbligatorio);
    }

    // ---------------- QUERY ----------------

    public List<Campo> getCampiBase() {
        return List.copyOf(campiBase);
    }

    public List<Campo> getCampiComuni() {
        return List.copyOf(campiComuni);
    }

    public List<Categoria> getCategorie() {
        return List.copyOf(categorie);
    }

    public boolean nomeEsistenteGlobale(String nome) {
        return campiBase.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome)) ||
                campiComuni.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
    }
}
