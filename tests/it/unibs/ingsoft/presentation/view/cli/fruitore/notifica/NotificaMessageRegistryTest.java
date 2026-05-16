package it.unibs.ingsoft.presentation.view.cli.fruitore.notifica;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.notifica.NotificaType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificaMessageRegistryTest {
    @Test
    void message_conResolverRegistrato_usaIlResolverAssociatoAlTipo() {
        Notifica notifica = Notifica.notificaStrutturata(
                NotificaType.PROPOSTA_RITIRATA,
                Map.of("titolo", "Torneo"));
        NotificaMessageRegistry registry = new NotificaMessageRegistry()
                .register(NotificaType.PROPOSTA_RITIRATA,
                        n -> "custom: " + n.payload().get("titolo"));

        assertEquals("custom: Torneo", registry.message(notifica));
    }

    @Test
    void cliDefault_mantieneIMessaggiCorrenti() {
        NotificaMessageRegistry registry = NotificaMessageRegistry.cliDefault();

        assertEquals(
                """
                La proposta "Torneo" e' stata CONFERMATA.
                Data: 25/12/2026
                Ora: 16:30
                Luogo: Brescia
                Quota: 10.50""",
                registry.message(Notifica.notificaStrutturata(
                        NotificaType.PROPOSTA_CONFERMATA,
                        Map.of(
                                "titolo", "Torneo",
                                "data", "25/12/2026",
                                "ora", "16:30",
                                "luogo", "Brescia",
                                "quota", "10.50"))));
        assertEquals(
                "La proposta \"Senza titolo\" e' stata RITIRATA dal configuratore.",
                registry.message(Notifica.notificaStrutturata(
                        NotificaType.PROPOSTA_RITIRATA,
                        Map.of())));
        assertEquals(
                "messaggio libero",
                registry.message(new Notifica("messaggio libero")));
    }

    @Test
    void message_conNotificaNull_restituisceStringaVuota() {
        assertEquals("", NotificaMessageRegistry.cliDefault().message(null));
    }
}
