package it.unibs.ingsoft.presentation.view.cli.fruitore.notifica;

import it.unibs.ingsoft.domain.model.notifica.Notifica;

@FunctionalInterface
public interface NotificaMessageResolver {
    String message(Notifica notifica);
}
