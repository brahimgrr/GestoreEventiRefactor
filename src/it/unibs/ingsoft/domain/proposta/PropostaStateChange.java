package it.unibs.ingsoft.domain.proposta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Voce immutabile della cronologia degli stati di una {@link Proposta}.
 * Registra lo stato raggiunto e la data in cui è avvenuta la transizione.
 */
public record PropostaStateChange(StatoProposta stato, LocalDate dataCambio) {
    @JsonCreator
    public PropostaStateChange(@JsonProperty("stato") StatoProposta stato,
                               @JsonProperty("dataCambio") LocalDate dataCambio) {
        this.stato = stato;
        this.dataCambio = dataCambio;
    }

    @Override
    public String toString() {
        return "PropostaStateChange{stato=" + stato + ", dataCambio=" + dataCambio + "}";
    }
}
