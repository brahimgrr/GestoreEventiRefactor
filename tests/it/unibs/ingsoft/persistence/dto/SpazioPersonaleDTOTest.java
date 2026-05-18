package it.unibs.ingsoft.persistence.dto;

import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaType;
import it.unibs.ingsoft.persistence.dto.SpazioPersonaleDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpazioPersonaleDTOTest {
    @Test
    void addNotifica_conNotificaNuova_aggiungeNotifica() {
        SpazioPersonaleDTO spazio = new SpazioPersonaleDTO();
        Notifica notifica = notifica("id-1");

        spazio.addNotifica(notifica);

        assertEquals(List.of(notifica), spazio.getNotifiche());
    }

    @Test
    void addNotifica_conNotificaGiaPresentePerId_nonAggiungeDuplicato() {
        SpazioPersonaleDTO spazio = new SpazioPersonaleDTO();
        spazio.addNotifica(notifica("id-1"));

        spazio.addNotifica(notifica("id-1"));

        assertEquals(1, spazio.getNotifiche().size());
    }

    @Test
    void removeNotifica_conNotificaPresente_rimuoveNotifica() {
        SpazioPersonaleDTO spazio = new SpazioPersonaleDTO();

        Notifica notifica = notifica("id-1");
        spazio.addNotifica(notifica);
        spazio.removeNotifica(notifica);

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void removeNotifica_conNotificaAssente_nonModificaLaLista() {
        SpazioPersonaleDTO spazio = new SpazioPersonaleDTO();

        spazio.removeNotifica(notifica("id-1"));

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void getNotifiche_quandoSiModificaListaRestituita_lanciaUnsupportedOperationException() {
        SpazioPersonaleDTO spazio = new SpazioPersonaleDTO();

        assertThrows(UnsupportedOperationException.class,
                () -> spazio.getNotifiche().add(notifica("id-1")));
    }

    @Test
    void fromJson_conListaNull_creaSpazioPersonaleSenzaNotifiche() {
        SpazioPersonaleDTO spazio = SpazioPersonaleDTO.fromJson(null);

        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void fromJson_conListaNotifiche_copiaLeNotifiche() {
        Notifica notifica = notifica("id-1");

        SpazioPersonaleDTO spazio = SpazioPersonaleDTO.fromJson(List.of(notifica));

        assertEquals(List.of(notifica), spazio.getNotifiche());
    }

    private Notifica notifica(String id) {
        return new Notifica(id, NotificaType.LEGACY_MESSAGGIO, Map.of(), "messaggio", LocalDateTime.now());
    }
}
