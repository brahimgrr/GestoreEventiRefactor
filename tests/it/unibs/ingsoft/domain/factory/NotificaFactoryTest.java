package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificaFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(NotificaFactory.getInstance(), NotificaFactory.getInstance());
    }

    @Test
    void creaNotifica_conMessaggioValido_creaNotificaConQuelMessaggio() {
        Notifica notifica = NotificaFactory.getInstance().creaNotifica("test");

        assertEquals("test", notifica.messaggio());
    }

    @Test
    void creaNotifica_conMessaggioNull_creaNotificaConQuelMessaggio() {
        Notifica notifica = NotificaFactory.getInstance().creaNotifica(null);

        assertNull(notifica.messaggio());
    }
}
