package it.unibs.ingsoft.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raccolta serializzabile delle proposte. Contiene proposte in qualsiasi stato.
 */
public final class Bacheca {
    private final List<Proposta> proposte;

    public Bacheca() {
        this.proposte = new ArrayList<>();
    }

    /**
     * Factory di deserializzazione Jackson.
     */
    @JsonCreator
    public static Bacheca fromJson(
            @JsonProperty("proposte") List<Proposta> proposte) {
        Bacheca bacheca = new Bacheca();
        if (proposte != null) bacheca.proposte.addAll(proposte);
        return bacheca;
    }

    public List<Proposta> getProposte() {
        return Collections.unmodifiableList(proposte);
    }

    /**
     * @pre p != null &amp;&amp; p.getStato() == StatoProposta.APERTA
     */
    public void addProposta(Proposta p) {
        proposte.add(p);
    }
}
