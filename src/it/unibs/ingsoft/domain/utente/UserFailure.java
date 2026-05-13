package it.unibs.ingsoft.domain.utente;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;

public sealed interface UserFailure extends DomainFailure
        permits UserFailure.UsernameInvalid {

    record UsernameInvalid() implements UserFailure {
    }
}
