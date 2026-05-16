package it.unibs.ingsoft.presentation.view.cli.fruitore.notifica;

import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.notifica.NotificaType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class NotificaMessageRegistry {
    private final Map<NotificaType, NotificaMessageResolver> resolvers =
            new EnumMap<>(NotificaType.class);

    public static NotificaMessageRegistry cliDefault() {
        NotificaMessageRegistry registry = new NotificaMessageRegistry();
        PropostaNotificaCliMessages.registerInto(registry);
        return registry;
    }

    public NotificaMessageRegistry register(NotificaType type, NotificaMessageResolver resolver) {
        resolvers.put(Objects.requireNonNull(type), Objects.requireNonNull(resolver));
        return this;
    }

    public String message(Notifica notifica) {
        if (notifica == null) {
            return "";
        }

        NotificaMessageResolver resolver = resolvers.get(notifica.type());
        if (resolver == null) {
            return legacyMessage(notifica);
        }
        return resolver.message(notifica);
    }

    static String legacyMessage(Notifica notifica) {
        return notifica.messaggio() == null ? "" : notifica.messaggio();
    }
}
