package it.unibs.ingsoft.domain.model.utente;

import it.unibs.ingsoft.domain.error.DomainFailure;

public sealed interface UserFailure extends DomainFailure
        permits UserFailure.UsernameInvalid {

    record UsernameInvalid() implements UserFailure {
    }
}
