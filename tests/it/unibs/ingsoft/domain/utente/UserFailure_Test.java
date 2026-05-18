package it.unibs.ingsoft.domain.utente;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
 Secondo me interfaccia è inutile da testare
  */
class UserFailure_Test {
    @Test
    void usernameInvalid_eDomainFailure() {
        assertInstanceOf(DomainFailure.class, new UserFailure.UsernameInvalid());
    }

    @Test
    void usernameInvalid_siComportaComeValueObject() {
        UserFailure.UsernameInvalid first = new UserFailure.UsernameInvalid();
        UserFailure.UsernameInvalid second = new UserFailure.UsernameInvalid();

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertEquals("UsernameInvalid[]", first.toString())
        );
    }
}
