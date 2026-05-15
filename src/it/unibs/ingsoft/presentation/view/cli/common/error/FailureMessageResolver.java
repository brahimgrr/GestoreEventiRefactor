package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.shared.error.Failure;

@FunctionalInterface
public interface FailureMessageResolver<T extends Failure> {
    String message(T failure, FailureMessageRegistry messages);
}
