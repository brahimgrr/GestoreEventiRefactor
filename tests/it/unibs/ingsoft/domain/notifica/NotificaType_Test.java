package it.unibs.ingsoft.domain.notifica;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/*
è un enum. Secondo me non ha senso testarlo ma codex ha generato quindi lascio temporaneamente
 */
class NotificaType_Test {
    @Test
    void values_quandoInvocato_restituisceTipiNotificaNelContrattoDelDominio() {
        assertArrayEquals(new NotificaType[]{
                NotificaType.PROPOSTA_CONFERMATA,
                NotificaType.PROPOSTA_ANNULLATA,
                NotificaType.PROPOSTA_RITIRATA,
                NotificaType.LEGACY_MESSAGGIO
        }, NotificaType.values());
    }

    @Test
    void valueOf_conNomeCostanteRestituisceCostante() {
        assertSame(NotificaType.PROPOSTA_RITIRATA, NotificaType.valueOf("PROPOSTA_RITIRATA"));
    }
}
