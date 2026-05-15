package it.unibs.ingsoft.domain.model.proposta;

import it.unibs.ingsoft.domain.error.ValidationError;

import java.time.LocalDate;
import java.util.List;

public record PropostaValidationOutcome(
        List<ValidationError> errori,
        LocalDate termineIscrizione,
        LocalDate dataEvento) {

    public PropostaValidationOutcome {
        errori = errori == null ? List.of() : List.copyOf(errori);
    }

    public boolean valida() {
        return errori.isEmpty();
    }
}
