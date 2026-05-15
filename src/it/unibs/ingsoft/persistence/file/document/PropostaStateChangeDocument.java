package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.proposta.PropostaStateChange;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;

import java.time.LocalDate;

public record PropostaStateChangeDocument(StatoProposta stato, LocalDate dataCambio) {
    public static PropostaStateChangeDocument fromDomain(PropostaStateChange change) {
        return new PropostaStateChangeDocument(change.stato(), change.dataCambio());
    }

    public PropostaStateChange toDomain() {
        return new PropostaStateChange(stato, dataCambio);
    }
}
