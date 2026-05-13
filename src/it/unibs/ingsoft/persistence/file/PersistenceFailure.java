package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.shared.error.InfrastructureFailure;

import java.nio.file.Path;

public sealed interface PersistenceFailure extends InfrastructureFailure
        permits PersistenceFailure.ReadFailed,
        PersistenceFailure.WriteFailed {

    record ReadFailed(Path path) implements PersistenceFailure {
    }

    record WriteFailed(Path path) implements PersistenceFailure {
    }
}
