package it.unibs.ingsoft.domain.utente;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UtenteFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() throws Exception {
        Field instance = UtenteFactory.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        assertSame(UtenteFactory.getInstance(), UtenteFactory.getInstance());
    }

    @Test
    void costruttorePrivato_creaIstanzaQuandoInvocatoViaReflection() throws Exception {
        Constructor<UtenteFactory> constructor = UtenteFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void creaConfiguratore_conUsernameValido_restituisceConfiguratoreConUsername() {
        Configuratore configuratore = UtenteFactory.getInstance().creaConfiguratore("admin");

        assertAll(
                () -> assertEquals("admin", configuratore.getUsername()),
                () -> assertInstanceOf(Configuratore.class, configuratore)
        );
    }

    @Test
    void creaConfiguratore_conUsernameBlank_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> UtenteFactory.getInstance().creaConfiguratore("   "));

        assertInstanceOf(UserFailure.UsernameInvalid.class, exception.failure());
    }

    @Test
    void creaConfiguratore_conUsernameNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> UtenteFactory.getInstance().creaConfiguratore(null));

        assertInstanceOf(UserFailure.UsernameInvalid.class, exception.failure());
    }

    @Test
    void creaFruitore_conUsernameValido_restituisceFruitoreConUsername() {
        Fruitore fruitore = UtenteFactory.getInstance().creaFruitore("mario");

        assertAll(
                () -> assertEquals("mario", fruitore.getUsername()),
                () -> assertInstanceOf(Fruitore.class, fruitore)
        );
    }

    @Test
    void creaFruitore_conUsernameBlank_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> UtenteFactory.getInstance().creaFruitore("   "));

        assertInstanceOf(UserFailure.UsernameInvalid.class, exception.failure());
    }

    @Test
    void creaFruitore_conUsernameNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> UtenteFactory.getInstance().creaFruitore(null));

        assertInstanceOf(UserFailure.UsernameInvalid.class, exception.failure());
    }
}
