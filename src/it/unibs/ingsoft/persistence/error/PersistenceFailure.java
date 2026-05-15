package it.unibs.ingsoft.persistence.error;

import java.nio.file.Path;

public sealed interface PersistenceFailure extends InfrastructureFailure
        permits PersistenceFailure.ReadFailed,
        PersistenceFailure.WriteFailed {

    record ReadFailed(Path path) implements PersistenceFailure {
    }

    record WriteFailed(Path path) implements PersistenceFailure {
    }
}
