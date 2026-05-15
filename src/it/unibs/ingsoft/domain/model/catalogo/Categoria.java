package it.unibs.ingsoft.domain.model.catalogo;

import it.unibs.ingsoft.domain.error.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Categoria {
    private final String nome;
    private final List<Campo> campiSpecifici;

    public Categoria(String nome) {
        if (nome == null || nome.isBlank())
            throw new DomainException(new CatalogFailure.CategoryNameInvalid());
        this.nome = nome.trim();
        this.campiSpecifici = new ArrayList<>();
    }

    public Categoria(Categoria oldCategoria) {
        this.nome = oldCategoria.nome;
        this.campiSpecifici = oldCategoria.campiSpecifici.stream()
                .map(Campo::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Ricostruisce una categoria da stato persistito gia' validato dal repository.
     * Non e' legato a JSON: vale anche per file, database o test fixtures.
     */
    public static Categoria rehydrate(String nome, List<Campo> campiSpecifici) {
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

    public void addCampoSpecifico(Campo campoSpecifico) {
        if (campoSpecifico.getTipo() != TipoCampo.SPECIFICO)
            throw new DomainException(new CatalogFailure.CategoryFieldNotSpecific());
        if (containsCampo(campoSpecifico.getNome()))
            throw new DomainException(new CatalogFailure.CategoryFieldDuplicated(nome, campoSpecifico.getNome()));

        campiSpecifici.add(campoSpecifico);
        campiSpecifici.sort(Comparator.comparing(Campo::getNome, String.CASE_INSENSITIVE_ORDER));
    }

    public boolean removeCampoSpecifico(String nomeCampo) {
        return campiSpecifici.removeIf(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

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
