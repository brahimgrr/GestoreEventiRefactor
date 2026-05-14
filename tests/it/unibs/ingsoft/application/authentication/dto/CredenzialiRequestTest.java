package it.unibs.ingsoft.application.authentication.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredenzialiRequestTest {
    @Test
    void costruttore_conUsernameEPassword_esponeValori() {
        CredenzialiRequest request = new CredenzialiRequest("mario", "pass1234");

        assertEquals("mario", request.username());
        assertEquals("pass1234", request.password());
    }
}
