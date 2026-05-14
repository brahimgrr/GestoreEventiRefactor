package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Notifica;
import it.unibs.ingsoft.domain.NotificaType;
import it.unibs.ingsoft.domain.Proposta;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificaFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(NotificaFactory.getInstance(), NotificaFactory.getInstance());
    }

    @Test
    void creaNotificaPropostaConfermata_conProposta_creaNotificaStrutturataConPayload() {
        Proposta proposta = new Proposta(new Categoria("Sport"), java.util.List.of(), java.util.List.of());
        proposta.aggiornaValoriCampi(Map.of(
                "Titolo", "Torneo",
                "Data", "25/12/2026",
                "Ora", "16:30",
                "Luogo", "Brescia",
                "Quota individuale", "10"
        ));

        Notifica notifica = NotificaFactory.getInstance().creaNotificaPropostaConfermata(proposta);

        assertAll(
                () -> assertEquals(NotificaType.PROPOSTA_CONFERMATA, notifica.type()),
                () -> assertEquals("Torneo", notifica.payload().get("titolo")),
                () -> assertEquals("25/12/2026", notifica.payload().get("data")),
                () -> assertEquals("16:30", notifica.payload().get("ora")),
                () -> assertEquals("Brescia", notifica.payload().get("luogo")),
                () -> assertEquals("10", notifica.payload().get("quota"))
        );
    }

    @Test
    void creaNotificaPropostaAnnullata_conProposta_creaNotificaDiTipoAnnullata() {
        Notifica notifica = NotificaFactory.getInstance()
                .creaNotificaPropostaAnnullata(new Proposta(new Categoria("Sport"), java.util.List.of(), java.util.List.of()));

        assertEquals(NotificaType.PROPOSTA_ANNULLATA, notifica.type());
    }

    @Test
    void creaNotificaPropostaRitirata_conProposta_creaNotificaDiTipoRitirata() {
        Notifica notifica = NotificaFactory.getInstance()
                .creaNotificaPropostaRitirata(new Proposta(new Categoria("Sport"), java.util.List.of(), java.util.List.of()));

        assertEquals(NotificaType.PROPOSTA_RITIRATA, notifica.type());
    }
}
