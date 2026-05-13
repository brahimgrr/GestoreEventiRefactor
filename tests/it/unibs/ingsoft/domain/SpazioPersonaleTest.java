package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.utente.SpazioPersonale;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpazioPersonaleTest {
    @Test
    void addNotifica_conNotificaNuova_aggiungeNotifica() {
        SpazioPersonale spazio = new SpazioPersonale();
        Notifica notifica = notifica("id-1");

        spazio.addNotifica(notifica);

        assertEquals(List.of(notifica), spazio.getNotifiche());
    }

    @Test
    void addNotifica_conNotificaGiaPresentePerId_nonAggiungeDuplicato() {
        SpazioPersonale spazio = new SpazioPersonale();
        spazio.addNotifica(notifica("id-1"));

        spazio.addNotifica(notifica("id-1"));

        assertEquals(1, spazio.getNotifiche().size());
    }

    @Test
    void removeNotifica_conNotificaPresente_rimuoveNotifica() {
        SpazioPersonale spazio = new SpazioPersonale();

        Notifica notifica = notifica("id-1");
        spazio.addNotifica(notifica);
        spazio.removeNotifica(notifica);

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void removeNotifica_conNotificaAssente_nonModificaLaLista() {
        SpazioPersonale spazio = new SpazioPersonale();

        spazio.removeNotifica(notifica("id-1"));

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void getNotifiche_quandoSiModificaListaRestituita_lanciaUnsupportedOperationException() {
        SpazioPersonale spazio = new SpazioPersonale();

        assertThrows(UnsupportedOperationException.class,
                () -> spazio.getNotifiche().add(notifica("id-1")));
    }

    @Test
    void fromJson_conListaNull_creaSpazioPersonaleSenzaNotifiche() {
        SpazioPersonale spazio = SpazioPersonale.fromJson(null);

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    private Notifica notifica(String id) {
        return new Notifica(id, NotificaType.LEGACY_MESSAGGIO, Map.of(), "messaggio", LocalDateTime.now());
    }
}
