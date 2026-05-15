package it.unibs.ingsoft.domain.policy.tipodato;

import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.policy.tipodato.strategies.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class TipoDatoValidationStrategyRegistry {
    private final Map<TipoDato, TipoDatoValidationStrategy> strategiePerTipo;

    private TipoDatoValidationStrategyRegistry(Map<TipoDato, TipoDatoValidationStrategy> strategiePerTipo) {
        EnumMap<TipoDato, TipoDatoValidationStrategy> strategie = new EnumMap<>(TipoDato.class);
        Objects.requireNonNull(strategiePerTipo).forEach((tipoDato, strategia) ->
                strategie.put(Objects.requireNonNull(tipoDato), Objects.requireNonNull(strategia)));
        this.strategiePerTipo = Collections.unmodifiableMap(strategie);
    }

    public static TipoDatoValidationStrategyRegistry standard() {
        EnumMap<TipoDato, TipoDatoValidationStrategy> strategie = new EnumMap<>(TipoDato.class);
        strategie.put(TipoDato.STRINGA, new StringaTipoDatoValidationStrategy());
        strategie.put(TipoDato.INTERO, new InteroTipoDatoValidationStrategy());
        strategie.put(TipoDato.INTERO_POSITIVO, new InteroPositivoTipoDatoValidationStrategy());
        strategie.put(TipoDato.DECIMALE, new DecimaleTipoDatoValidationStrategy());
        strategie.put(TipoDato.DATA, new DataTipoDatoValidationStrategy());
        strategie.put(TipoDato.ORA, new OraTipoDatoValidationStrategy());
        strategie.put(TipoDato.BOOLEANO, new BooleanoTipoDatoValidationStrategy());
        return of(strategie);
    }

    public static TipoDatoValidationStrategyRegistry of(Map<TipoDato, TipoDatoValidationStrategy> strategiePerTipo) {
        return new TipoDatoValidationStrategyRegistry(strategiePerTipo);
    }

    public TipoDatoValidationStrategy getStrategy(TipoDato tipo) {
        TipoDatoValidationStrategy strategia = strategiePerTipo.get(Objects.requireNonNull(tipo));
        if (strategia == null) {
            throw new IllegalArgumentException("Nessuna strategia di validazione configurata per " + tipo);
        }
        return strategia;
    }
}
