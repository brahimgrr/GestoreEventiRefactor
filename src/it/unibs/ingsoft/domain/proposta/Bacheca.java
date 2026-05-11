package it.unibs.ingsoft.domain.proposta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raccolta serializzabile delle proposte. Contiene proposte in qualsiasi stato.
 */
public final class Bacheca {
    private final List<Proposta> proposte;
    private final PropostaIdentityPolicy duplicatePolicy = PropostaIdentityPolicy.DEFAULT;

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

    public Optional<Proposta> findByChiaveDuplicato(String chiaveDuplicato) {
        if (chiaveDuplicato == null) {
            return Optional.empty();
        }

        return proposte.stream()
                .filter(p -> chiaveDuplicato.equals(duplicatePolicy.chiaveDuplicato(p)))
                .findFirst();
    }

    public Optional<Proposta> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return proposte.stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst();
    }

    public boolean containsChiaveDuplicato(String chiaveDuplicato) {
        return findByChiaveDuplicato(chiaveDuplicato).isPresent();
    }

    public Proposta findSameIdentityAs(Proposta proposta) {
        if (proposta == null) {
            throw new DomainException(DomainErrorCode.PROPOSTA_NON_TROVATA);
        }

        Optional<Proposta> byId = findById(proposta.getId());
        if (byId.isPresent()) {
            return byId.get();
        }

        String chiave = duplicatePolicy.chiaveDuplicato(proposta);
        return findByChiaveDuplicato(chiave)
                .orElseThrow(() -> new DomainException(DomainErrorCode.PROPOSTA_NON_TROVATA, chiave));
    }

    public void addProposta(Proposta p) {
        proposte.add(Objects.requireNonNull(p));
    }
}
