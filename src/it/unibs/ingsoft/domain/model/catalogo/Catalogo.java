package it.unibs.ingsoft.domain.model.catalogo;

import it.unibs.ingsoft.domain.error.DomainException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Catalogo {
    private final List<Campo> campiBase = new ArrayList<>();
    private final List<Campo> campiComuni = new ArrayList<>();
    private final List<Categoria> categorie = new ArrayList<>();

    private boolean campiBaseFissati;

    /**
     * Ricostruisce il catalogo da stato persistito gia' validato dal repository.
     * Non e' legato a JSON: vale anche per file, database o test fixtures.
     */
    public static Catalogo rehydrate(List<Campo> campiBase,
                                     boolean campiBaseFissati,
                                     List<Campo> campiComuni,
                                     List<Categoria> categorie) {
        Catalogo catalogo = new Catalogo();
        if (campiBase != null) {
            catalogo.campiBase.addAll(campiBase);
        }
        catalogo.campiBaseFissati = campiBaseFissati;
        if (campiComuni != null) {
            catalogo.campiComuni.addAll(campiComuni);
        }
        if (categorie != null) {
            catalogo.categorie.addAll(categorie);
        }
        return catalogo;
    }

    public void fissareCampiBase(List<Campo> base, List<Campo> extra) {
        if (campiBaseFissati) {
            throw new DomainException(new CatalogFailure.BaseFieldsAlreadyFixed());
        }

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

                if (nomeEsistenteGlobale(c.getNome())) {
                    throw new DomainException(new CatalogFailure.FieldDuplicated(c.getNome()));
                }

                campiBase.add(c);
            }
        }

        campiBaseFissati = true;
    }

    private void addNomeUnico(Set<String> set, String nome) {
        if (!set.add(nome.toLowerCase())) {
            throw new DomainException(new CatalogFailure.NameDuplicated(nome));
        }
    }

    public boolean isCampiBaseFissati() {
        return campiBaseFissati;
    }

    public void addCampoComune(Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome())) {
            throw new DomainException(new CatalogFailure.FieldDuplicated(campo.getNome()));
        }

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

    public Categoria addCategoria(String nome) {
        if (findCategoria(nome).isPresent()) {
            throw new DomainException(new CatalogFailure.CategoryDuplicated(nome));
        }

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

    public void addCampoSpecifico(String categoria, Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome())) {
            throw new DomainException(new CatalogFailure.FieldDuplicated(campo.getNome()));
        }

        getCategoriaOrThrow(categoria).addCampoSpecifico(campo);
    }

    public boolean removeCampoSpecifico(String categoria, String nome) {
        return getCategoriaOrThrow(categoria).removeCampoSpecifico(nome);
    }

    public boolean updateCampoSpecifico(String categoria, String nome, boolean obbligatorio) {
        return getCategoriaOrThrow(categoria)
                .setObbligatorietaCampoSpecifico(nome, obbligatorio);
    }

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
