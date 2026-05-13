package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.shared.error.Failure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FailureMessageRegistry {
    private final Map<Class<? extends Failure>, FailureMessageResolver<? extends Failure>> resolvers =
            new LinkedHashMap<>();

    public static FailureMessageRegistry cliDefault() {
        FailureMessageRegistry registry = new FailureMessageRegistry();
        AuthenticationFailureCliMessages.registerInto(registry);
        CatalogFailureCliMessages.registerInto(registry);
        ImportFailureCliMessages.registerInto(registry);
        PersistenceFailureCliMessages.registerInto(registry);
        ProposalFailureCliMessages.registerInto(registry);
        ProposalValidationFailureCliMessages.registerInto(registry);
        TypeValidationFailureCliMessages.registerInto(registry);
        UserFailureCliMessages.registerInto(registry);
        return registry;
    }

    public <T extends Failure> FailureMessageRegistry register(
            Class<T> type,
            FailureMessageResolver<? super T> resolver) {
        resolvers.put(Objects.requireNonNull(type), Objects.requireNonNull(resolver));
        return this;
    }

    public String message(Failure failure) {
        if (failure == null) {
            return "";
        }
        FailureMessageResolver<Failure> resolver = resolverFor(failure);
        if (resolver == null) {
            return failure.code();
        }
        return resolver.message(failure, this);
    }

    @SuppressWarnings("unchecked")
    private FailureMessageResolver<Failure> resolverFor(Failure failure) {
        Class<?> type = failure.getClass();
        while (type != null) {
            FailureMessageResolver<? extends Failure> resolver = resolvers.get(type);
            if (resolver != null) {
                return (FailureMessageResolver<Failure>) resolver;
            }
            type = type.getSuperclass(); // chain of responsability
        }
        for (Map.Entry<Class<? extends Failure>, FailureMessageResolver<? extends Failure>> entry : resolvers.entrySet()) {
            if (entry.getKey().isInstance(failure)) {
                return (FailureMessageResolver<Failure>) entry.getValue();
            }
        }
        return null;
    }
}
