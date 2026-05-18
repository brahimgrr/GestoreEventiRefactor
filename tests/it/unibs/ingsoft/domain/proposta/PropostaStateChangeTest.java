package it.unibs.ingsoft.domain.proposta;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PropostaStateChangeTest {
    @Test
    void costruttore_conValoriPresenti_memorizzaStatoEData() {
        LocalDate data = LocalDate.of(2026, 5, 15);

        PropostaStateChange change = new PropostaStateChange(StatoProposta.APERTA, data);

        assertAll(
                () -> assertEquals(StatoProposta.APERTA, change.stato()),
                () -> assertEquals(data, change.dataCambio())
        );
    }

    @Test
    void costruttore_conValoriNull_liAccettaComeRecordSemplice() {
        PropostaStateChange change = new PropostaStateChange(null, null);

        assertAll(
                () -> assertNull(change.stato()),
                () -> assertNull(change.dataCambio())
        );
    }

    @Test
    void equalityHashCodeEToString_siComportanoComeValueObject() {
        LocalDate data = LocalDate.of(2026, 5, 15);
        PropostaStateChange first = new PropostaStateChange(StatoProposta.BOZZA, data);
        PropostaStateChange second = new PropostaStateChange(StatoProposta.BOZZA, data);

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertEquals("PropostaStateChange{stato=BOZZA, dataCambio=2026-05-15}", first.toString())
        );
    }
}
