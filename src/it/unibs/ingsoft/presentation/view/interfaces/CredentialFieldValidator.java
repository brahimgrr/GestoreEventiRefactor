package it.unibs.ingsoft.presentation.view.interfaces;

/**
 * Validatore di un singolo campo credenziale usato dalla view per feedback inline.
 */
@FunctionalInterface
public interface CredentialFieldValidator {
    void validate(String value);
}
