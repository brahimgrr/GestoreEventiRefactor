package it.unibs.ingsoft.domain.policy.tipodato.strategies;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategy;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class BooleanoTipoDatoValidationStrategy implements TipoDatoValidationStrategy {
    private static final Set<String> VALORI_SI = Set.of("s", "si", "s\u00ec", "true");
    private static final Set<String> VALORI_NO = Set.of("n", "no", "false");

    @Override
    public Optional<ValidationError> valida(String value) {
        String lower = value.trim().toLowerCase(Locale.ROOT);
        if (VALORI_SI.contains(lower) || VALORI_NO.contains(lower)) {
            return Optional.empty();
        }
        return Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidBoolean()));
    }
}
