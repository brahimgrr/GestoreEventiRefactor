package it.unibs.ingsoft.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.shared.error.DomainException;

import java.util.*;

public final class CatalogoDTO {
    private final List<Campo> campiBase = new ArrayList<>();
    private final List<Campo> campiComuni = new ArrayList<>();
    private final List<Categoria> categorie = new ArrayList<>();

    private boolean campiBaseFissati;

    /**
     * Factory di deserializzazione Jackson.
     */
    @JsonCreator
    public static CatalogoDTO fromJson(
            @JsonProperty("campiBase") List<Campo> campiBase,
            @JsonProperty("campiBaseFissati") boolean campiBaseFissati,
            @JsonProperty("campiComuni") List<Campo> campiComuni,
            @JsonProperty("categorie") List<Categoria> categorie
    ) {
        CatalogoDTO d = new CatalogoDTO();
        if (campiBase != null) d.campiBase.addAll(campiBase);
        if (campiBaseFissati) d.campiBaseFissati = true;
        if (campiComuni != null) d.campiComuni.addAll(campiComuni);
        if (categorie != null) d.categorie.addAll(categorie);
        return d;
    }
    // ---------------- CAMPI BASE ----------------

    public void fissareCampiBase(List<Campo> base, List<Campo> extra) {
        if (campiBaseFissati)
            throw new DomainException(new CatalogFailure.BaseFieldsAlreadyFixed());

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
                    throw new DomainException(new CatalogFailure.FieldDuplicated(c.getNome()));

                campiBase.add(c);
            }
        }

        campiBaseFissati = true;
    }

    private void addNomeUnico(Set<String> set, String nome) {
        if (!set.add(nome.toLowerCase()))
            throw new DomainException(new CatalogFailure.NameDuplicated(nome));
    }

    public boolean isCampiBaseFissati() {
        return campiBaseFissati;
    }

    // ---------------- CAMPI COMUNI ----------------

    public void addCampoComune(Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome()))
            throw new DomainException(new CatalogFailure.FieldDuplicated(campo.getNome()));

        campiComuni.add(campo);
    }

    public boolean removeCampoComune(String nome) {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

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

    public Categoria addCategoria(String nome) {
        if (findCategoria(nome).isPresent())
            throw new DomainException(new CatalogFailure.CategoryDuplicated(nome));

        Categoria c = new Categoria(nome);
        categorie.add(c);
        return c;
    }

    public boolean removeCategoria(String nome) {
        return categorie.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    public Categoria getCategoriaOrThrow(String nome) {
        return findCategoria(nome)
                .orElseThrow(() -> new DomainException(new CatalogFailure.CategoryNotFound(nome)));
    }

    private Optional<Categoria> findCategoria(String nome) {
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    // ---------------- CAMPI SPECIFICI ----------------

    public void addCampoSpecifico(String categoria, Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome()))
            throw new DomainException(new CatalogFailure.FieldDuplicated(campo.getNome()));

        getCategoriaOrThrow(categoria).addCampoSpecifico(campo);
    }

    public boolean removeCampoSpecifico(String categoria, String nome) {
        return getCategoriaOrThrow(categoria).removeCampoSpecifico(nome);
    }

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
