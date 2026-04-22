package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Categoria di iniziativa ricreativa. Ogni categoria ha un nome univoco e una lista
 * di campi specifici, mantenuta ordinata alfabeticamente per nome.
 *
 * <p>Invariante: {@code campiSpecifici} è sempre ordinata per nome (case-insensitive).</p>
 */
public final class Categoria {
    private final String nome;
    private final List<Campo> campiSpecifici;

    public Categoria(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome della categoria non può essere vuoto.");
        this.nome = nome.trim();
        this.campiSpecifici = new ArrayList<>();
    }

    public Categoria(Categoria oldCategoria) {
        this.nome = oldCategoria.nome;
        this.campiSpecifici = oldCategoria.campiSpecifici.stream().map(Campo::new).toList();
    }

    /**
     * Factory di deserializzazione Jackson — ripristina nome e campi specifici.
     */
    @JsonCreator
    public static Categoria fromJson(
            @JsonProperty("nome") String nome,
            @JsonProperty("campiSpecifici") List<Campo> campiSpecifici) {
        Categoria cat = new Categoria(nome);
        if (campiSpecifici != null)
            cat.campiSpecifici.addAll(campiSpecifici);
        return cat;
    }

    public String getNome() {
        return nome;
    }

    public List<Campo> getCampiSpecifici() {
        return Collections.unmodifiableList(campiSpecifici);
    }

    /**
     * Aggiunge un campo specifico alla categoria.
     *
     * @throws IllegalArgumentException se il tipo è errato o il nome è duplicato
     * @pre campoSpecifico.getTipo() == TipoCampo.SPECIFICO
     * @pre nessun campo specifico ha già lo stesso nome (case-insensitive)
     */
    public void addCampoSpecifico(Campo campoSpecifico) {
        if (campoSpecifico.getTipo() != TipoCampo.SPECIFICO)
            throw new IllegalArgumentException("Solo campi di tipo SPECIFICO possono essere aggiunti a una categoria.");
        if (containsCampo(campoSpecifico.getNome()))
            throw new IllegalArgumentException(
                    "La categoria \"" + nome + "\" ha già un campo chiamato \"" + campoSpecifico.getNome() + "\".");

        campiSpecifici.add(campoSpecifico);
        campiSpecifici.sort(Comparator.comparing(Campo::getNome, String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * @return {@code true} se rimosso, {@code false} se non trovato
     */
    public boolean removeCampoSpecifico(String nomeCampo) {
        return campiSpecifici.removeIf(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    /**
     * Aggiorna il flag {@code obbligatorio} del campo specifico indicato.
     * Sostituisce il {@link Campo} esistente (immutabile) con una nuova istanza
     * tramite {@link Campo#withObbligatorio(boolean)}.
     *
     * @return {@code true} se aggiornato, {@code false} se non trovato
     */
    public boolean setObbligatorietaCampoSpecifico(String nomeCampo, boolean obbligatorio) {
        for (int i = 0; i < campiSpecifici.size(); i++) {
            if (campiSpecifici.get(i).getNome().equalsIgnoreCase(nomeCampo)) {
                campiSpecifici.set(i, campiSpecifici.get(i).withObbligatorio(obbligatorio));
                return true;
            }
        }
        return false;
    }

    private boolean containsCampo(String nomeCampo) {
        return campiSpecifici.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    /**
     * Uguaglianza case-insensitive sul nome.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Categoria)) return false;
        return nome.equalsIgnoreCase(((Categoria) obj).nome);
    }

    @Override
    public int hashCode() {
        return nome.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return nome;
    }
}
