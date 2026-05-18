package it.unibs.ingsoft.domain.proposta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/*
è un enum. Secondo me non ha senso testarlo ma codex ha generato quindi lascio temporaneamente
 */
class EsitoTransizioneProposta_Test {
    @Test
    void values_quandoInvocato_restituisceEsitiNelContrattoDelDominio() {
        assertArrayEquals(new EsitoTransizioneProposta[]{
                EsitoTransizioneProposta.NESSUNA,
                EsitoTransizioneProposta.CONFERMATA,
                EsitoTransizioneProposta.ANNULLATA,
                EsitoTransizioneProposta.CONCLUSA,
                EsitoTransizioneProposta.RITIRATA
        }, EsitoTransizioneProposta.values());
    }

    @Test
    void valueOf_conNomeCostanteRestituisceCostante() {
        assertSame(EsitoTransizioneProposta.CONFERMATA, EsitoTransizioneProposta.valueOf("CONFERMATA"));
    }
}
