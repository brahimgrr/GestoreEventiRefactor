package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PropostaStoreDocument(List<PropostaDocument> proposte) {
    public PropostaStoreDocument {
        proposte = proposte == null ? List.of() : List.copyOf(proposte);
    }

    public static PropostaStoreDocument empty() {
        return new PropostaStoreDocument(List.of());
    }

    public List<Proposta> findAll() {
        return proposte.stream().map(PropostaDocument::toDomain).toList();
    }

    public Optional<Proposta> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return proposte.stream()
                .filter(proposta -> id.equals(proposta.id()))
                .findFirst()
                .map(PropostaDocument::toDomain);
    }

    public List<Proposta> findOpen() {
        return findAll().stream().filter(Proposta::isAperta).toList();
    }

    public List<Proposta> findByState(StatoProposta stato) {
        return findAll().stream()
                .filter(proposta -> proposta.getStato() == stato)
                .toList();
    }

    public PropostaStoreDocument save(Proposta proposta) {
        List<PropostaDocument> next = new ArrayList<>(proposte);
        PropostaDocument document = PropostaDocument.fromDomain(proposta);
        for (int i = 0; i < next.size(); i++) {
            if (proposta.getId().equals(next.get(i).id())) {
                next.set(i, document);
                return new PropostaStoreDocument(next);
            }
        }
        next.add(document);
        return new PropostaStoreDocument(next);
    }
}
