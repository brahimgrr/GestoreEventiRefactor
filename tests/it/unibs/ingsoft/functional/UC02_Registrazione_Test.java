package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.authentication.AuthenticationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UC02_Registrazione_Test {
    @Test
    void scenarioPrincipale_credenzialiValide_registraAccountPersistente() {
        FunctionalTestSupport.AuthenticationContext context = FunctionalTestSupport.authenticationContext();

        context.service().registraNuovoConfiguratore("nuovoAdmin", "pass1234");

        assertAll(
                () -> assertTrue(context.service().login("nuovoadmin", "pass1234").isPresent()),
                () -> assertEquals(1, context.repository().saveCount())
        );
    }

    /*
    Non ha senso testare dato che tanto ne piu ne meno non posso fare nulla
     */
    @Test
    void scenarioAlternativo3a6a_attoreAnnullaOperazione_nonRegistraAccount() {
        FunctionalTestSupport.AuthenticationContext context = FunctionalTestSupport.authenticationContext();

        assertAll(
                () -> assertFalse(context.service().esisteUsername("annullato")),
                () -> assertEquals(0, context.repository().saveCount())
        );
    }

    @Test
    void scenarioAlternativo4a_usernameGiaRegistrato_mantieneArchivioInvariato() {
        AuthenticationService service = FunctionalTestSupport.authenticationContext().service();
        service.registraNuovoConfiguratore("utente", "pass1234");

        assertThrows(IllegalStateException.class,
                () -> service.registraNuovoFruitore("UTENTE", "pass5678"));
    }

    @Test
    void scenarioAlternativo4b_usernameNonValido_segnalaErrore() {
        AuthenticationService service = FunctionalTestSupport.authenticationContext().service();

        assertThrows(IllegalStateException.class, () -> service.validaNuovoUsername("ab"));
    }

    @Test
    void scenarioAlternativo7a_passwordNonValida_segnalaErrore() {
        AuthenticationService service = FunctionalTestSupport.authenticationContext().service();

        assertThrows(IllegalStateException.class, () -> service.validaNuovaPassword("abc"));
    }

    /*
    STESSA COSA DI QUELLO SOPRA. Username e password VENGONO VALIDATI MA NON USATI
    PERCHE ANNULLATI A META STRADA QUINDI PERDE SENSO IL TEST
     */
    @Test
    void scenarioAlternativo9a_attoreNonConfermaRegistrazione_nonSalvaCredenziali() {
        FunctionalTestSupport.AuthenticationContext context = FunctionalTestSupport.authenticationContext();
        context.service().validaNuovoUsername("nonconfermato");
        context.service().validaNuovaPassword("pass1234");

        assertEquals(0, context.repository().saveCount());
    }
}
