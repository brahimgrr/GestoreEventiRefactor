package it.unibs.ingsoft.application.authentication.dto;

/**
 * Dati raccolti dalla UI per login e registrazione.
 */
public record CredenzialiRequest(String username, String password) {
}
