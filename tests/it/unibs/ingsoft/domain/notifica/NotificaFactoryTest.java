package it.unibs.ingsoft.domain.notifica;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificaFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(NotificaFactory.getInstance(), NotificaFactory.getInstance());
    }

    @Test
    void costruttorePrivato_creaIstanzaQuandoInvocatoViaReflection() throws Exception {
        Constructor<NotificaFactory> constructor = NotificaFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
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
                .creaNotificaPropostaRitirata(propostaConValoriCompleti());

        assertAll(
                () -> assertEquals(NotificaType.PROPOSTA_RITIRATA, notifica.type()),
                () -> assertPayloadCompleto(notifica)
        );
    }

    @Test
    void creaNotificaPropostaConfermata_conProposta_creaNotificaDiTipoConfermataConPayload() {
        Notifica notifica = NotificaFactory.getInstance()
                .creaNotificaPropostaConfermata(propostaConValoriCompleti());

        assertAll(
                () -> assertEquals(NotificaType.PROPOSTA_CONFERMATA, notifica.type()),
                () -> assertPayloadCompleto(notifica)
        );
    }

    @Test
    void creaNotificaPropostaAnnullata_conProposta_creaNotificaDiTipoAnnullataConPayload() {
        Notifica notifica = NotificaFactory.getInstance()
                .creaNotificaPropostaAnnullata(propostaConValoriCompleti());

        assertAll(
                () -> assertEquals(NotificaType.PROPOSTA_ANNULLATA, notifica.type()),
                () -> assertPayloadCompleto(notifica)
        );
    }

    @Test
    void creaNotificheProposta_conValoriMancanti_usaStringheVuoteNelPayload() {
        Notifica notifica = NotificaFactory.getInstance()
                .creaNotificaPropostaConfermata(new Proposta(new Categoria("Sport"), java.util.List.of(), java.util.List.of()));

        assertAll(
                () -> assertEquals("", notifica.payload().get("titolo")),
                () -> assertEquals("", notifica.payload().get("data")),
                () -> assertEquals("", notifica.payload().get("ora")),
                () -> assertEquals("", notifica.payload().get("luogo")),
                () -> assertEquals("", notifica.payload().get("quota"))
        );
    }

    @Test
    void creaNotificaProposta_conPropostaNull_lanciaNullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> NotificaFactory.getInstance().creaNotificaPropostaConfermata(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> NotificaFactory.getInstance().creaNotificaPropostaAnnullata(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> NotificaFactory.getInstance().creaNotificaPropostaRitirata(null))
        );
    }

    private Proposta propostaConValoriCompleti() {
        Proposta proposta = new Proposta(new Categoria("Sport"), java.util.List.of(), java.util.List.of());
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "Torneo",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_QUOTA, "10.50"));
        return proposta;
    }

    private void assertPayloadCompleto(Notifica notifica) {
        assertAll(
                () -> assertEquals("Torneo", notifica.payload().get("titolo")),
                () -> assertEquals("25/12/2026", notifica.payload().get("data")),
                () -> assertEquals("16:30", notifica.payload().get("ora")),
                () -> assertEquals("Brescia", notifica.payload().get("luogo")),
                () -> assertEquals("10.50", notifica.payload().get("quota")),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> notifica.payload().put("x", "y"))
        );
    }
}
