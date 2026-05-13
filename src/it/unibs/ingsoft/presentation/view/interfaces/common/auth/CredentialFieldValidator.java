package it.unibs.ingsoft.presentation.view.interfaces.common.auth;

import it.unibs.ingsoft.domain.shared.error.Failure;

import java.util.Optional;

/**
 * Validatore di un singolo campo credenziale usato dalla view per feedback inline.
 */
@FunctionalInterface
public interface CredentialFieldValidator {
    Optional<Failure> validate(String value);
}
