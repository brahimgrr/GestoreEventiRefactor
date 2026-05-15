package it.unibs.ingsoft.domain.policy.tipodato;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;

import java.util.Objects;
import java.util.Optional;

/**
 * Valida una stringa grezza rispetto al {@link TipoDato} atteso.
 */
public final class TipoDatoValidator {
    public static final TipoDatoValidator INSTANCE = standard();

    private final TipoDatoValidationStrategyRegistry strategyRegistry;

    public TipoDatoValidator(TipoDatoValidationStrategyRegistry strategyRegistry) {
        this.strategyRegistry = Objects.requireNonNull(strategyRegistry);
    }

    public static TipoDatoValidator standard() {
        return new TipoDatoValidator(TipoDatoValidationStrategyRegistry.standard());
    }

    public Optional<ValidationError> validate(String input, TipoDato tipo) {
        Objects.requireNonNull(tipo);
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        return strategyRegistry.getStrategy(tipo).valida(input);
    }
}
