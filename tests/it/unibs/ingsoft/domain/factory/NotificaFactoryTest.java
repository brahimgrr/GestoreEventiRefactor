package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
import it.unibs.ingsoft.domain.notifica.NotificaType;
import it.unibs.ingsoft.domain.proposta.Proposta;
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

    @Test
    void creaNotificaPropostaRitirata_conProposta_creaNotificaDiTipoRitirata() {
        Notifica notifica = NotificaFactory.getInstance()
                .creaNotificaPropostaRitirata(new Proposta(new Categoria("Sport"), java.util.List.of(), java.util.List.of()));

        assertEquals(NotificaType.PROPOSTA_RITIRATA, notifica.type());
    }
}
