package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
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
            throw new DomainException(DomainErrorCode.CATEGORIA_NOME_NON_VALIDO);
        this.nome = nome.trim();
        this.campiSpecifici = new ArrayList<>();
    }

    public Categoria(Categoria oldCategoria) {
        this.nome = oldCategoria.nome;
        this.campiSpecifici = oldCategoria.campiSpecifici.stream()
                .map(Campo::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

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

    public void addCampoSpecifico(Campo campoSpecifico) {
        if (campoSpecifico.getTipo() != TipoCampo.SPECIFICO)
            throw new DomainException(DomainErrorCode.CATEGORIA_CAMPO_NON_SPECIFICO);
        if (containsCampo(campoSpecifico.getNome()))
            throw new DomainException(DomainErrorCode.CATEGORIA_CAMPO_DUPLICATO, nome, campoSpecifico.getNome());

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
